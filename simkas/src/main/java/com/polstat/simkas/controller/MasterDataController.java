// File: src/main/java/com/polstat/simkas/controller/MasterDataController.java
package com.polstat.simkas.controller;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.service.MasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk Fitur Manajemen Data Master
 * (Kategori, Kelas, Angkatan)
 */
@RestController
@RequestMapping("/api/master")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    // === KATEGORI ===

    @PostMapping("/kategori")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<KategoriDto> createKategori(@RequestBody KategoriRequest request) {
        return new ResponseEntity<>(masterDataService.createKategori(request), HttpStatus.CREATED);
    }

    @GetMapping("/kategori")
    @PreAuthorize("isAuthenticated()") // Semua user boleh lihat
    public ResponseEntity<List<KategoriDto>> getAllKategori() {
        return ResponseEntity.ok(masterDataService.getAllKategori());
    }

    @PutMapping("/kategori/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<KategoriDto> updateKategori(@PathVariable Long id, @RequestBody KategoriRequest request) {
        return ResponseEntity.ok(masterDataService.updateKategori(id, request));
    }

    @DeleteMapping("/kategori/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<?> deleteKategori(@PathVariable Long id) {
        masterDataService.deleteKategori(id);
        return ResponseEntity.ok("Kategori deleted successfully");
    }

    // === KELAS ===

    @PostMapping("/kelas")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<KelasDto> createKelas(@RequestBody KelasRequest request) {
        return new ResponseEntity<>(masterDataService.createKelas(request), HttpStatus.CREATED);
    }

    @GetMapping("/kelas")
    @PreAuthorize("isAuthenticated()") // Semua user boleh lihat
    public ResponseEntity<List<KelasDto>> getAllKelas() {
        return ResponseEntity.ok(masterDataService.getAllKelas());
    }

    @GetMapping("/kelas/by-angkatan/{angkatanId}")
    @PreAuthorize("isAuthenticated()") // Semua user boleh lihat
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
        return ResponseEntity.ok("Kelas deleted successfully");
    }

    // === ANGKATAN ===

    @PostMapping("/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<AngkatanDto> createAngkatan(@RequestBody AngkatanRequest request) {
        return new ResponseEntity<>(masterDataService.createAngkatan(request), HttpStatus.CREATED);
    }

    @GetMapping("/angkatan")
    @PreAuthorize("isAuthenticated()") // Semua user boleh lihat
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
        return ResponseEntity.ok("Angkatan deleted successfully");
    }
}