package com.polstat.simkas.controller;

import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.dto.TransaksiResponse;
import com.polstat.simkas.entity.Transaksi;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.service.TransaksiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transaksi")
public class TransaksiController {

    private final TransaksiService transaksiService;

    public TransaksiController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    // =======================
    // ADMIN_ANGKATAN - lihat transaksi seluruh angkatan / filter kelas
    // =======================
    @GetMapping("/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> getTransaksiAngkatan(Authentication authentication,
                                                  @RequestParam(required = false) Long kelasId) {
        String username = authentication.getName();
        User admin = transaksiService.getUserByUsername(username);

        if (admin.getAngkatan() == null) {
            return ResponseEntity.badRequest().body("Admin angkatan tidak memiliki angkatan yang terdaftar");
        }

        List<TransaksiResponse> list = transaksiService.findByAngkatan(admin.getAngkatan().getId(), kelasId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    // =======================
    // ADMIN_ANGKATAN - tambah transaksi untuk seangkatan
    // =======================
    @PostMapping("/angkatan/kelas")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> createTransaksiAngkatan(@RequestBody TransaksiRequest req, Authentication authentication) {
        String username = authentication.getName();
        Transaksi saved = transaksiService.createTransaksiAdminAngkatan(req, username);
        return ResponseEntity.ok(toResponse(saved));
    }

    // =======================
    // BENDAHARA_KELAS - lihat / tambah transaksi kelasnya sendiri
    // =======================
    @GetMapping("/kelas")
    @PreAuthorize("hasAuthority('BENDAHARA_KELAS')")
    public ResponseEntity<?> getTransaksiKelas(Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.findByKelasUsername(username)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/kelas")
    @PreAuthorize("hasAuthority('BENDAHARA_KELAS')")
    public ResponseEntity<?> createTransaksiBendahara(@RequestBody TransaksiRequest req, Authentication authentication) {
        String username = authentication.getName();
        Transaksi saved = transaksiService.createTransaksiByRole(req, username);
        return ResponseEntity.ok(toResponse(saved));
    }

    // =======================
    // ANGGOTA / BENDAHARA / ADMIN lihat transaksi user
    // =======================
    @GetMapping("/me/{userId}")
    @PreAuthorize("hasAnyAuthority('ANGGOTA','BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> byUser(@PathVariable Long userId, Authentication authentication) {
        String username = authentication.getName();
        User actor = transaksiService.getUserByUsername(username);

        // cek role & batasan akses
        if (actor.getRole().getId() == 3 && !actor.getId().equals(userId)) {
            return ResponseEntity.status(403).body("Kamu tidak boleh lihat transaksi orang lain!");
        }
        if (actor.getRole().getId() == 2) {
            User targetUser = transaksiService.getUserById(userId);
            if (!targetUser.getKelas().getId().equals(actor.getKelas().getId())) {
                return ResponseEntity.status(403).body("Bendahara hanya bisa lihat transaksi anggota di kelasnya sendiri!");
            }
        }
        if (actor.getRole().getId() == 1) {
            User targetUser = transaksiService.getUserById(userId);
            if (!targetUser.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
                return ResponseEntity.status(403).body("Admin angkatan hanya bisa lihat transaksi anggota di angkatannya sendiri!");
            }
        }

        List<TransaksiResponse> list = transaksiService.findByUserIdAndActor(userId, actor)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // =======================
    // UPDATE, DELETE, VALIDATE (ADMIN_ANGKATAN / BENDAHARA_KELAS)
    // =======================
    @PutMapping("/kelas/{id}")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody TransaksiRequest req,
                                    Authentication authentication) {
        String username = authentication.getName();
        Transaksi updated = transaksiService.updateTransaksiByRole(id, req, username);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/kelas/{id}")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        transaksiService.deleteTransaksiByRole(id, username);
        return ResponseEntity.ok("deleted");
    }

    @PutMapping("/validate/{id}")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> validate(@PathVariable Long id,
                                      @RequestParam String status,
                                      Authentication authentication) {
        String username = authentication.getName();
        Transaksi updated = transaksiService.validateTransaksi(id, status, username);
        return ResponseEntity.ok(toResponse(updated));
    }

    // =======================
// LAPORAN
// =======================
    @GetMapping("/laporan/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> laporanAngkatan(@RequestParam(required = false) Integer bulan,
                                             @RequestParam Integer tahun,
                                             @RequestParam(required = false) String jenisTransaksi,
                                             Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.laporanAngkatan(username, bulan, tahun, jenisTransaksi)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/laporan/kelas/admin")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> laporanKelasAdmin(@RequestParam(required = false) Integer bulan,
                                               @RequestParam Integer tahun,
                                               @RequestParam(required = false) String jenisTransaksi,
                                               @RequestParam(required = false) Long kelasId,
                                               Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.laporanKelas(username, bulan, tahun, jenisTransaksi, kelasId, true)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/laporan/kelas")
    @PreAuthorize("hasAuthority('BENDAHARA_KELAS')")
    public ResponseEntity<?> laporanKelasBendahara(@RequestParam(required = false) Integer bulan,
                                                   @RequestParam Integer tahun,
                                                   @RequestParam(required = false) String jenisTransaksi,
                                                   Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.laporanKelas(username, bulan, tahun, jenisTransaksi, null, false)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/laporan/user")
    @PreAuthorize("hasAnyAuthority('BENDAHARA_KELAS','ADMIN_ANGKATAN')")
    public ResponseEntity<?> laporanUser(@RequestParam Long userId,
                                         @RequestParam(required = false) Integer bulan,
                                         @RequestParam Integer tahun,
                                         @RequestParam(required = false) String jenisTransaksi,
                                         Authentication authentication) {
        String username = authentication.getName();
        List<TransaksiResponse> list = transaksiService.laporanUser(userId, bulan, tahun, jenisTransaksi, username)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }


    // =======================
    // Helper: Transaksi -> DTO
    // =======================
    private TransaksiResponse toResponse(Transaksi t) {
        TransaksiResponse r = new TransaksiResponse();
        r.setId(t.getId());
        r.setIdUser(t.getUser() != null ? t.getUser().getId() : null);
        r.setIdInputBy(t.getInputBy() != null ? t.getInputBy().getId() : null);
        r.setIdKelas(t.getKelas() != null ? t.getKelas().getId() : null);
        r.setIdAngkatan(t.getAngkatan() != null ? t.getAngkatan().getId() : null);
        r.setIdKategori(t.getKategori() != null ? t.getKategori().getId() : null);
        r.setBulanKas(t.getBulanKas());
        r.setTahunKas(t.getTahunKas());
        r.setNominal(t.getNominal());
        r.setTanggalBayar(t.getTanggalBayar());
        r.setKeterangan(t.getKeterangan());
        r.setJenisTransaksi(t.getJenisTransaksi());
        r.setStatusValidasi(t.getStatusValidasi() != null ? t.getStatusValidasi().name() : null);
        return r;
    }
}

