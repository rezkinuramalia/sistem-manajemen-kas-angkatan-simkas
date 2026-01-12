package com.polstat.simkas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polstat.simkas.dto.HistoryTransaksi;
import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.dto.TransaksiResponse; // ✅ Fix: Gunakan TransaksiResponse
import com.polstat.simkas.entity.Transaksi;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.service.TransaksiService;
import com.polstat.simkas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal; // ✅ Fix: Import BigDecimal
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transaksi")
public class TransaksiController {

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Path fileStorageLocation = Paths.get("uploads/bukti-bayar").toAbsolutePath().normalize();

    public TransaksiController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Tidak bisa membuat folder upload!", ex);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTransaksi(
            @RequestPart("data") String transaksiJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        try {
            TransaksiRequest req = objectMapper.readValue(transaksiJson, TransaksiRequest.class);
            String username = authentication.getName();

            if (file != null && !file.isEmpty()) {
                String originalName = file.getOriginalFilename();
                String ext = "";
                if(originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String fileName = UUID.randomUUID().toString() + ext;
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                req.setBuktiBayar(fileName);
            }

            Transaksi saved = transaksiService.createTransaksiByRole(req, username);
            return ResponseEntity.ok(toResponse(saved));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Gagal upload file atau parsing data: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<HistoryTransaksi>> getMyHistory(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<HistoryTransaksi> history = transaksiService.getHistoryByUserId(user.getId());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> getTransaksiAngkatan(Authentication authentication, @RequestParam(required = false) Long kelasId) {
        String username = authentication.getName();
        User admin = userService.getUserByUsername(username);
        if (admin.getAngkatan() == null) return ResponseEntity.badRequest().body("Admin angkatan tidak memiliki angkatan");

        List<TransaksiResponse> list = transaksiService.findByAngkatan(admin.getAngkatan().getId(), kelasId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/kelas")
    @PreAuthorize("hasAuthority('BENDAHARA_KELAS')")
    public ResponseEntity<?> getTransaksiKelas(Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.findByKelasUsername(username)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ANGGOTA','BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> byUser(@PathVariable Long userId, Authentication authentication) {
        String username = authentication.getName();
        User actor = userService.getUserByUsername(username);
        if (actor.getRole().getId() == 3 && !actor.getId().equals(userId)) {
            return ResponseEntity.status(403).body("Akses ditolak.");
        }
        List<TransaksiResponse> list = transaksiService.findByUserIdAndActor(userId, actor)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TransaksiRequest req, Authentication authentication) {
        String username = authentication.getName();
        Transaksi updated = transaksiService.updateTransaksiByRole(id, req, username);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        transaksiService.deleteTransaksiByRole(id, username);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PutMapping("/validate/{id}")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> validate(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        String username = authentication.getName();
        Transaksi updated = transaksiService.validateTransaksi(id, status, username);
        return ResponseEntity.ok(toResponse(updated));
    }

    // ===============================================
    //  ENDPOINT BARU UNTUK VALIDASI (ANDROID)
    // ===============================================

    // Endpoint Validasi (Return DTO agar sesuai dengan frontend)
    @PutMapping("/{id}/validasi")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<TransaksiResponse> validasiTransaksi( // ✅ Fix: TransaksiResponse
                                                                @PathVariable Long id,
                                                                @RequestParam String status
    ) {
        return ResponseEntity.ok(transaksiService.validasiTransaksi(id, status));
    }

    // Endpoint Get Pending by Kategori
    @GetMapping("/pending/kategori/{idKategori}")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<List<TransaksiResponse>> getPendingByKategori(@PathVariable Long idKategori) { // ✅ Fix: TransaksiResponse
        return ResponseEntity.ok(transaksiService.getPendingTransaksiByKategori(idKategori));
    }

    // Endpoint Get Total Pemasukan Valid
    @GetMapping("/total/kategori/{idKategori}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalPemasukan(@PathVariable Long idKategori) { // ✅ Fix: Sudah benar (BigDecimal)
        return ResponseEntity.ok(transaksiService.hitungTotalPemasukanKategori(idKategori));
    }

    private TransaksiResponse toResponse(Transaksi t) {
        TransaksiResponse r = new TransaksiResponse();
        r.setId(t.getId());
        r.setIdUser(t.getUser() != null ? t.getUser().getId() : null);
        r.setNominal(t.getNominal());
        r.setTanggalBayar(t.getTanggalBayar());
        r.setKeterangan(t.getKeterangan());
        r.setJenisTransaksi(t.getJenisTransaksi());
        r.setStatusValidasi(t.getStatusValidasi() != null ? t.getStatusValidasi().name() : null);
        r.setBuktiBayar(t.getBuktiBayar());
        return r;
    }
}