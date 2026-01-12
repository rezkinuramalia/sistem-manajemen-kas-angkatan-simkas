package com.polstat.simkas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polstat.simkas.dto.HistoryTransaksi;
import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.dto.TransaksiResponse;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transaksi")// Hapus "/api" jika di application.properties sudah ada context-path, atau sesuaikan
public class TransaksiController {

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper; // Untuk parsing JSON manual

    // Lokasi folder penyimpanan gambar
    private final Path fileStorageLocation = Paths.get("uploads/bukti-bayar").toAbsolutePath().normalize();

    public TransaksiController() {
        // Buat folder upload otomatis saat aplikasi jalan
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Tidak bisa membuat folder upload!", ex);
        }
    }

    // =================================================================
    //  1. CREATE TRANSAKSI (UPLOAD BUKTI BAYAR) - UNTUK SEMUA ROLE
    // =================================================================
    // Endpoint ini menangani Multipart (File + Data JSON)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTransaksi(
            @RequestPart("data") String transaksiJson,  // Data JSON diterima sebagai String
            @RequestPart(value = "file", required = false) MultipartFile file, // File Gambar (Opsional)
            Authentication authentication) {

        try {
            // 1. Convert JSON String ke Object TransaksiRequest
            TransaksiRequest req = objectMapper.readValue(transaksiJson, TransaksiRequest.class);
            String username = authentication.getName();

            // 2. Proses File Gambar (Jika ada)
            String fileName = null;
            if (file != null && !file.isEmpty()) {
                // Generate nama file unik (misal: uuid_bukti.jpg)
                String originalName = file.getOriginalFilename();
                String ext = "";
                if(originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                fileName = UUID.randomUUID().toString() + ext;

                // Simpan file ke folder
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // Set nama file ke request agar disimpan service
                req.setBuktiBayar(fileName); // Pastikan DTO TransaksiRequest punya field ini
            }

            // 3. Panggil Service
            // Gunakan method createTransaksiByRole agar logic role (Bendahara/Mahasiswa) dihandle service
            Transaksi saved = transaksiService.createTransaksiByRole(req, username);

            return ResponseEntity.ok(toResponse(saved));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Gagal upload file atau parsing data: " + e.getMessage());
        }
    }

    // =================================================================
    //  2. GET HISTORY TRANSAKSI USER SENDIRI (Fitur Android History)
    // =================================================================
    @GetMapping("/history")
    public ResponseEntity<List<HistoryTransaksi>> getMyHistory(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());

        // Panggil service untuk ambil transaksi user ini & convert ke DTO History
        // Pastikan transaksiService punya method getHistoryByUserId
        List<HistoryTransaksi> history = transaksiService.getHistoryByUserId(user.getId());

        return ResponseEntity.ok(history);
    }

    // =================================================================
    //  3. ADMIN ANGKATAN - LIHAT SEMUA TRANSAKSI
    // =================================================================
    @GetMapping("/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> getTransaksiAngkatan(Authentication authentication,
                                                  @RequestParam(required = false) Long kelasId) {
        String username = authentication.getName();
        User admin = userService.getUserByUsername(username);

        if (admin.getAngkatan() == null) {
            return ResponseEntity.badRequest().body("Admin angkatan tidak memiliki angkatan yang terdaftar");
        }

        List<TransaksiResponse> list = transaksiService.findByAngkatan(admin.getAngkatan().getId(), kelasId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    // =================================================================
    //  4. BENDAHARA KELAS - LIHAT TRANSAKSI KELASNYA
    // =================================================================
    @GetMapping("/kelas")
    @PreAuthorize("hasAuthority('BENDAHARA_KELAS')")
    public ResponseEntity<?> getTransaksiKelas(Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.findByKelasUsername(username)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // =================================================================
    //  5. LIHAT TRANSAKSI USER TERTENTU (DETAIL)
    // =================================================================
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ANGGOTA','BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> byUser(@PathVariable Long userId, Authentication authentication) {
        String username = authentication.getName();
        User actor = userService.getUserByUsername(username);

        // Validasi: Anggota biasa gaboleh liat punya orang lain
        if (actor.getRole().getId() == 3 && !actor.getId().equals(userId)) {
            return ResponseEntity.status(403).body("Akses ditolak.");
        }

        List<TransaksiResponse> list = transaksiService.findByUserIdAndActor(userId, actor)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // =================================================================
    //  6. UPDATE, DELETE, VALIDATE (Admin & Bendahara)
    // =================================================================
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

    // =======================
    // Helper: Transaksi -> DTO
    // =======================
    private TransaksiResponse toResponse(Transaksi t) {
        TransaksiResponse r = new TransaksiResponse();
        r.setId(t.getId());
        r.setIdUser(t.getUser() != null ? t.getUser().getId() : null);
        r.setNominal(t.getNominal());
        r.setTanggalBayar(t.getTanggalBayar()); // FIX: Langsung masukkan Instant
        r.setKeterangan(t.getKeterangan());
        r.setJenisTransaksi(t.getJenisTransaksi());
        r.setStatusValidasi(t.getStatusValidasi() != null ? t.getStatusValidasi().name() : null);
        r.setBuktiBayar(t.getBuktiBayar());

        // Info Kategori/Wadah
        if(t.getKategori() != null) {
            // Bisa tambahkan nama kategori di TransaksiResponse jika mau
            // r.setNamaKategori(t.getKategori().getNama());
        }

        return r;
    }
}