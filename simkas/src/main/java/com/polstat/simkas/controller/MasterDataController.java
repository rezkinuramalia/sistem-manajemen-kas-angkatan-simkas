package com.polstat.simkas.controller;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    // =======================================================
    // 1. MANAJEMEN KATEGORI (WADAH KAS)
    // =======================================================

    // === TAMBAHAN WAJIB AGAR LIST MUNCUL DI ANDROID ===
    @GetMapping("/kategori")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KategoriDto>> getAllKategoriGeneral() {
        // Method ini akan kita buat di Service di langkah nomor 3
        return ResponseEntity.ok(masterDataService.getAllKategoriSesuaiRole());
    }

    @PostMapping("/kategori")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<KategoriDto> createKategori(@RequestBody KategoriRequest request) {
        return ResponseEntity.ok(masterDataService.createKategori(request));
    }

    @GetMapping("/kategori/managed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KategoriDto>> getKategoriManaged() {
        return ResponseEntity.ok(masterDataService.getKategoriManagedByUser());
    }

    @GetMapping("/kategori/payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KategoriDto>> getKategoriForPayment() {
        return ResponseEntity.ok(masterDataService.getKategoriForPaymentByUser());
    }

    @PutMapping("/kategori/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<KategoriDto> updateKategori(@PathVariable Long id, @RequestBody KategoriRequest request) {
        return ResponseEntity.ok(masterDataService.updateKategori(id, request));
    }

    @DeleteMapping("/kategori/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
    public ResponseEntity<?> deleteKategori(@PathVariable Long id) {
        masterDataService.deleteKategori(id);
        return ResponseEntity.ok("Kategori berhasil dihapus");
    }

    // =======================================================
    // 2. MANAJEMEN KELAS & ANGKATAN
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