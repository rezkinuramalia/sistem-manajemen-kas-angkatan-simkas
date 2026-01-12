//package com.polstat.simkas.controller;
//
//import com.polstat.simkas.dto.KategoriDto;
//import com.polstat.simkas.dto.KategoriRequest;
//import com.polstat.simkas.entity.Kategori;
//import com.polstat.simkas.repository.KategoriRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/kategori")
//public class KategoriController {
//
//    private final KategoriRepository kategoriRepository;
//
//    public KategoriController(KategoriRepository kategoriRepository) {
//        this.kategoriRepository = kategoriRepository;
//    }
//
//    // ==========================================
//    // 1. LIHAT SEMUA KATEGORI (Untuk Dropdown di Android)
//    // ==========================================
//    @GetMapping
//    public ResponseEntity<List<KategoriDto>> getAllKategori() {
//        // Ambil data dari database
//        List<Kategori> list = kategoriRepository.findAll();
//
//        // Convert Entity ke DTO (Menggunakan Builder sesuai kode kamu)
//        List<KategoriDto> dtos = list.stream().map(k -> KategoriDto.builder()
//                .id(k.getId())
//                .nama(k.getNama())                // Sesuai entity kamu: 'nama'
//                .keterangan(k.getKeterangan())    // Sesuai entity kamu: 'keterangan'
//                .build()
//        ).collect(Collectors.toList());
//
//        return ResponseEntity.ok(dtos);
//    }
//
//    // ==========================================
//    // 2. BUAT KATEGORI BARU (Admin Angkatan / Bendahara)
//    // ==========================================
//    @PostMapping
//    @PreAuthorize("hasAnyAuthority('ADMIN_ANGKATAN', 'BENDAHARA_KELAS')")
//    public ResponseEntity<?> createKategori(@RequestBody KategoriRequest req) {
//        // Validasi input
//        if (req.getNama() == null || req.getNama().isEmpty()) {
//            return ResponseEntity.badRequest().body("Nama kategori tidak boleh kosong");
//        }
//
//        // Mapping dari Request ke Entity (Pakai Builder)
//        Kategori k = Kategori.builder()
//                .nama(req.getNama())              // Mapping field 'nama'
//                .keterangan(req.getKeterangan())  // Mapping field 'keterangan'
//                .createdAt(Instant.now())         // Set waktu sekarang
//                .build();
//
//        // Simpan ke database
//        Kategori saved = kategoriRepository.save(k);
//
//        // Balikin response berupa DTO
//        return ResponseEntity.ok(KategoriDto.builder()
//                .id(saved.getId())
//                .nama(saved.getNama())
//                .keterangan(saved.getKeterangan())
//                .build());
//    }
//}