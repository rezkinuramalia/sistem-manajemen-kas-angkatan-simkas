// File: src/main/java/com/polstat/simkas/controller/MasterDataController.java
package com.polstat.simkas.controller;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk Fitur Manajemen Data Master
 * Mengelola: Kategori (Wadah), Kelas, dan Angkatan.
 * Terhubung langsung dengan MasterDataService.
 */
@RestController
@RequestMapping("/api/master")
public class MasterDataController {

    private final MasterDataService masterDataService;

    // Kita cukup inject Service saja, karena Repository sudah diurus oleh Service
    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    // =======================================================
    // 1. MANAJEMEN KATEGORI (WADAH KAS)
    // =======================================================

    // Membuat Wadah Baru (Admin -> Level Angkatan, Bendahara -> Level Kelas)
    @PostMapping("/kategori")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<KategoriDto> createKategori(@RequestBody KategoriRequest request) {
        return ResponseEntity.ok(masterDataService.createKategori(request));
    }

    // [ENDPOINT BERANDA]
    // Menampilkan wadah yang DIBUAT/DIKELOLA oleh user.
    // Admin Angkatan -> Lihat List Wadah Angkatan
    // Bendahara Kelas -> Lihat List Wadah Kelas
    @GetMapping("/kategori/managed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KategoriDto>> getKategoriManaged() {
        return ResponseEntity.ok(masterDataService.getKategoriManagedByUser());
    }

    // [ENDPOINT MENU BAYAR]
    // Menampilkan wadah TUJUAN BAYAR.
    // Bendahara Kelas -> Lihat List Wadah Angkatan (Tempat dia setor)
    // Mahasiswa -> Lihat List Wadah Kelas (Tempat dia bayar kas)
    @GetMapping("/kategori/payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KategoriDto>> getKategoriForPayment() {
        return ResponseEntity.ok(masterDataService.getKategoriForPaymentByUser());
    }

    // Update Nama/Keterangan Wadah
    @PutMapping("/kategori/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<KategoriDto> updateKategori(@PathVariable Long id, @RequestBody KategoriRequest request) {
        return ResponseEntity.ok(masterDataService.updateKategori(id, request));
    }

    // Hapus Wadah
    @DeleteMapping("/kategori/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<?> deleteKategori(@PathVariable Long id) {
        masterDataService.deleteKategori(id);
        return ResponseEntity.ok("Kategori berhasil dihapus");
    }

    // =======================================================
    // 2. MANAJEMEN KELAS (CRUD)
    // =======================================================

    @PostMapping("/kelas")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<KelasDto> createKelas(@RequestBody KelasRequest request) {
        return ResponseEntity.ok(masterDataService.createKelas(request));
    }

    @GetMapping("/kelas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KelasDto>> getAllKelas() {
        return ResponseEntity.ok(masterDataService.getAllKelas());
    }

    // Filter Kelas berdasarkan Angkatan tertentu (Opsional)
    @GetMapping("/kelas/by-angkatan/{angkatanId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KelasDto>> getKelasByAngkatan(@PathVariable Long angkatanId) {
        return ResponseEntity.ok(masterDataService.getKelasByAngkatan(angkatanId));
    }

    @PutMapping("/kelas/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<KelasDto> updateKelas(@PathVariable Long id, @RequestBody KelasRequest request) {
        return ResponseEntity.ok(masterDataService.updateKelas(id, request));
    }

    @DeleteMapping("/kelas/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> deleteKelas(@PathVariable Long id) {
        masterDataService.deleteKelas(id);
        return ResponseEntity.ok("Kelas berhasil dihapus");
    }

    // =======================================================
    // 3. MANAJEMEN ANGKATAN (CRUD)
    // =======================================================

    @PostMapping("/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<AngkatanDto> createAngkatan(@RequestBody AngkatanRequest request) {
        return ResponseEntity.ok(masterDataService.createAngkatan(request));
    }

    @GetMapping("/angkatan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AngkatanDto>> getAllAngkatan() {
        return ResponseEntity.ok(masterDataService.getAllAngkatan());
    }

    @PutMapping("/angkatan/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<AngkatanDto> updateAngkatan(@PathVariable Long id, @RequestBody AngkatanRequest request) {
        return ResponseEntity.ok(masterDataService.updateAngkatan(id, request));
    }

    @DeleteMapping("/angkatan/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> deleteAngkatan(@PathVariable Long id) {
        masterDataService.deleteAngkatan(id);
        return ResponseEntity.ok("Angkatan berhasil dihapus");
    }
}