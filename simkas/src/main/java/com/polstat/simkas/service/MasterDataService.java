// File: src/main/java/com/polstat/simkas/service/MasterDataService.java
package com.polstat.simkas.service;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk logika bisnis Fitur Manajemen Data Master
 * Mengatur Logika Multi-Level:
 * 1. Admin Angkatan -> Mengelola Wadah Angkatan.
 * 2. Bendahara Kelas -> Mengelola Wadah Kelas & Membayar ke Wadah Angkatan.
 * 3. Mahasiswa -> Membayar ke Wadah Kelas.
 */
@Service
@Transactional
public class MasterDataService {

    private final KategoriRepository kategoriRepository;
    private final KelasRepository kelasRepository;
    private final AngkatanRepository angkatanRepository;
    private final UserRepository userRepository;

    // KONSTANTA ID ANGKATAN 65 (Sesuai instruksi: ID-nya 2)
    private static final Long ID_ANGKATAN_65 = 2L;

    public MasterDataService(KategoriRepository kategoriRepository,
                             KelasRepository kelasRepository,
                             AngkatanRepository angkatanRepository,
                             UserRepository userRepository) {
        this.kategoriRepository = kategoriRepository;
        this.kelasRepository = kelasRepository;
        this.angkatanRepository = angkatanRepository;
        this.userRepository = userRepository;
    }

    // =======================================================
    // 1. KATEGORI (WADAH KAS) - LOGIKA BARU
    // =======================================================

    // Method Membuat Wadah (Create)
    public KategoriDto createKategori(KategoriRequest request) {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        String level;
        Long idKelasPemilik = null;

        // Logic penentuan Level Wadah
        if ("BENDAHARA_KELAS".equals(role)) {
            level = "KELAS";
            // Validasi: Bendahara wajib punya kelas
            if (user.getKelas() == null) {
                throw new RuntimeException("Bendahara tidak terdaftar di kelas manapun!");
            }
            idKelasPemilik = user.getKelas().getId();
        } else if ("ADMIN_ANGKATAN".equals(role)) {
            level = "ANGKATAN";
            idKelasPemilik = null; // Level angkatan tidak butuh ID Kelas
        } else {
            throw new RuntimeException("Role ini tidak diizinkan membuat wadah.");
        }

        Kategori kategori = Kategori.builder()
                .nama(request.getNama())
                .keterangan(request.getKeterangan())
                .level(level)
                .idKelasPemilik(idKelasPemilik)
                .build();

        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    // ----------------------------------------------------------------------
    // [PENTING] LOGIKA UNTUK BERANDA (MANAGEMENT)
    // Menampilkan wadah yang DIBUAT/DIKELOLA oleh user tersebut
    // ----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriManagedByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        if ("ADMIN_ANGKATAN".equals(role)) {
            // Admin Angkatan: Melihat semua wadah level ANGKATAN (yang dia buat)
            return kategoriRepository.findByLevel("ANGKATAN")
                    .stream().map(this::toDto).collect(Collectors.toList());

        } else if ("BENDAHARA_KELAS".equals(role)) {
            // Bendahara Kelas: Melihat wadah level KELAS milik kelasnya sendiri (yang dia buat)
            if (user.getKelas() == null) return List.of();
            return kategoriRepository.findByLevelAndIdKelasPemilik("KELAS", user.getKelas().getId())
                    .stream().map(this::toDto).collect(Collectors.toList());

        } else {
            // Mahasiswa: Tidak punya fitur "Mengelola Wadah" (Halaman Beranda kosong/beda logika)
            return List.of();
        }
    }

    // ----------------------------------------------------------------------
    // [PENTING] LOGIKA UNTUK MENU BAYAR (PAYMENT DESTINATION)
    // Menampilkan wadah kemana user harus MEMBAYAR
    // ----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriForPaymentByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        if ("BENDAHARA_KELAS".equals(role)) {
            // Bendahara Kelas: MEMBAYAR ke Wadah ANGKATAN (Setoran)
            return kategoriRepository.findByLevel("ANGKATAN")
                    .stream().map(this::toDto).collect(Collectors.toList());

        } else if ("ANGGOTA".equals(role)) { // Mahasiswa
            // Mahasiswa: MEMBAYAR ke Wadah KELAS mereka sendiri (Iuran)
            if (user.getKelas() == null) return List.of();
            return kategoriRepository.findByLevelAndIdKelasPemilik("KELAS", user.getKelas().getId())
                    .stream().map(this::toDto).collect(Collectors.toList());

        } else {
            // Admin Angkatan: Tidak membayar ke siapa-siapa
            return List.of();
        }
    }

    // Method Update
    public KategoriDto updateKategori(Long id, KategoriRequest request) {
        Kategori kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori not found"));
        kategori.setNama(request.getNama());
        kategori.setKeterangan(request.getKeterangan());
        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    // Method Delete
    public void deleteKategori(Long id) {
        kategoriRepository.deleteById(id);
    }

    // =======================================================
    // 2. KELAS (Dibatasi Khusus Angkatan 65 / ID 2)
    // =======================================================

    public KelasDto createKelas(KelasRequest request) {
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65)
                .orElseThrow(() -> new RuntimeException("Angkatan 65 (ID 2) tidak ditemukan!"));

        Kelas kelas = Kelas.builder()
                .kode(request.getKode())
                .nama(request.getNama())
                .angkatan(angkatan)
                .build();
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    @Transactional(readOnly = true)
    public List<KelasDto> getAllKelas() {
        return kelasRepository.findByAngkatanId(ID_ANGKATAN_65)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // [INI METHOD YANG HILANG SEBELUMNYA]
    @Transactional(readOnly = true)
    public List<KelasDto> getKelasByAngkatan(Long angkatanId) {
        return kelasRepository.findByAngkatanId(angkatanId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public KelasDto updateKelas(Long id, KelasRequest request) {
        Kelas kelas = kelasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kelas not found"));
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65)
                .orElseThrow(() -> new RuntimeException("Angkatan 65 not found"));

        kelas.setKode(request.getKode());
        kelas.setNama(request.getNama());
        kelas.setAngkatan(angkatan);
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    public void deleteKelas(Long id) {
        kelasRepository.deleteById(id);
    }

    // =======================================================
    // 3. ANGKATAN (Filter Khusus ID 2)
    // =======================================================

    public AngkatanDto createAngkatan(AngkatanRequest request) {
        Angkatan angkatan = Angkatan.builder()
                .tahun(request.getTahun())
                .nama(request.getNama())
                .build();
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    @Transactional(readOnly = true)
    public List<AngkatanDto> getAllAngkatan() {
        return angkatanRepository.findAll().stream()
                .filter(a -> a.getId().equals(ID_ANGKATAN_65))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AngkatanDto updateAngkatan(Long id, AngkatanRequest request) {
        Angkatan angkatan = angkatanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Angkatan not found"));
        angkatan.setTahun(request.getTahun());
        angkatan.setNama(request.getNama());
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    public void deleteAngkatan(Long id) {
        angkatanRepository.deleteById(id);
    }

    // =======================================================
    // HELPER METHODS
    // =======================================================

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByNim(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User login tidak ditemukan"));
    }

    private KategoriDto toDto(Kategori k) {
        return KategoriDto.builder()
                .id(k.getId())
                .nama(k.getNama())
                .keterangan(k.getKeterangan())
                .level(k.getLevel())
                // nominal dihapus
                .build();
    }

    private KelasDto toDto(Kelas k) {
        Long angkatanId = (k.getAngkatan() != null) ? k.getAngkatan().getId() : null;
        String namaAngkatan = (k.getAngkatan() != null && k.getAngkatan().getTahun() != null)
                ? k.getAngkatan().getTahun().toString()
                : "-";

        return KelasDto.builder()
                .id(k.getId())
                .kode(k.getKode())
                .nama(k.getNama())
                .angkatanId(angkatanId)
                .namaAngkatan(namaAngkatan)
                .build();
    }

    private AngkatanDto toDto(Angkatan a) {
        return AngkatanDto.builder().id(a.getId()).tahun(a.getTahun()).nama(a.getNama()).build();
    }
}