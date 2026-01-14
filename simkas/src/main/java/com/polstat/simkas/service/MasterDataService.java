package com.polstat.simkas.service;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MasterDataService {

    private final KategoriRepository kategoriRepository;
    private final KelasRepository kelasRepository;
    private final AngkatanRepository angkatanRepository;
    private final UserRepository userRepository;
    private final TransaksiRepository transaksiRepository;

    // Hardcoded ID untuk Angkatan default (sesuaikan jika perlu)
    private static final Long ID_ANGKATAN_65 = 2L;

    public MasterDataService(KategoriRepository kategoriRepository,
                             KelasRepository kelasRepository,
                             AngkatanRepository angkatanRepository,
                             UserRepository userRepository,
                             TransaksiRepository transaksiRepository) {
        this.kategoriRepository = kategoriRepository;
        this.kelasRepository = kelasRepository;
        this.angkatanRepository = angkatanRepository;
        this.userRepository = userRepository;
        this.transaksiRepository = transaksiRepository;
    }

    // ==================================================================================
    // 1. COMPARATOR / SORTING LOGIC
    // ==================================================================================
    // Logic:
    // 1. Status Aktif (True) selalu di ATAS.
    // 2. Status Nonaktif (False) selalu di BAWAH.
    // 3. Jika status sama, urutkan berdasarkan ID (Terbaru di atas).
    private final Comparator<KategoriDto> kategoriSorter = (k1, k2) -> {
        boolean a1 = k1.getAktif() != null ? k1.getAktif() : true;
        boolean a2 = k2.getAktif() != null ? k2.getAktif() : true;

        if (a1 != a2) {
            // Boolean.compare(true, false) return 1, tapi kita mau true duluan (descending logic untuk boolean)
            return Boolean.compare(a2, a1);
        }
        // Jika status sama, urutkan ID descending
        return k2.getId().compareTo(k1.getId());
    };

    // ==================================================================================
    // 2. LOGIC MENU BERANDA (Kelola Wadah Sendiri)
    // ==================================================================================
    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriManagedByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        List<Kategori> rawList = new ArrayList<>();

        if ("BENDAHARA_KELAS".equals(role)) {
            // Bendahara Kelas hanya melihat wadah level KELAS milik kelasnya sendiri
            if (user.getKelas() != null) {
                rawList = kategoriRepository.findByLevelAndIdKelasPemilik("KELAS", user.getKelas().getId());
            }
        } else if ("ADMIN_ANGKATAN".equals(role)) {
            // Admin Angkatan melihat wadah level ANGKATAN
            rawList = kategoriRepository.findByLevel("ANGKATAN");
        }
        // Role lain (Anggota) tidak mengelola wadah, jadi return kosong (atau sesuaikan kebutuhan)

        // Convert ke DTO dan Sort
        List<KategoriDto> listDto = rawList.stream().map(this::toDto).collect(Collectors.toList());
        listDto.sort(kategoriSorter);
        return listDto;
    }

    // ==================================================================================
    // 3. LOGIC MENU BAYAR (Wadah yang Harus Dibayar)
    // ==================================================================================
    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriForPaymentByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        List<Kategori> rawList = new ArrayList<>();

        if ("BENDAHARA_KELAS".equals(role)) {
            // Bendahara membayar tagihan ke ANGKATAN (Admin)
            rawList = kategoriRepository.findByLevel("ANGKATAN");
        } else {
            // Anggota/Mahasiswa/Sekretaris membayar tagihan ke KELAS (Bendahara)
            if (user.getKelas() != null) {
                rawList = kategoriRepository.findByLevelAndIdKelasPemilik("KELAS", user.getKelas().getId());
            }
        }
        // Admin Angkatan list bayarnya kosong (karena dia role tertinggi)

        // Convert ke DTO dan Sort
        List<KategoriDto> listDto = rawList.stream().map(this::toDto).collect(Collectors.toList());
        listDto.sort(kategoriSorter);
        return listDto;
    }

    // ==================================================================================
    // 4. CRUD KATEGORI (CREATE & UPDATE STATUS)
    // ==================================================================================

    // Create Kategori dengan Level & Pemilik Otomatis
    public KategoriDto createKategori(KategoriRequest request) {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        String level;
        Long idKelasPemilik = null;

        if ("BENDAHARA_KELAS".equals(role)) {
            level = "KELAS";
            if (user.getKelas() == null) {
                throw new RuntimeException("Bendahara tidak memiliki kelas yang valid.");
            }
            idKelasPemilik = user.getKelas().getId();
        } else if ("ADMIN_ANGKATAN".equals(role)) {
            level = "ANGKATAN";
            idKelasPemilik = null; // Level angkatan tidak terikat kelas spesifik
        } else {
            throw new RuntimeException("Role Anda tidak diizinkan membuat tempat pembayaran.");
        }

        Kategori kategori = Kategori.builder()
                .nama(request.getNama())
                .keterangan(request.getKeterangan())
                .nominal(request.getNominal() != null ? request.getNominal() : BigDecimal.ZERO)
                .level(level)
                .idKelasPemilik(idKelasPemilik)
                .aktif(true) // Default selalu aktif saat dibuat
                .build();

        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    // Update Data Kategori (Nama/Nominal/Ket)
    public KategoriDto updateKategori(Long id, KategoriRequest request) {
        Kategori kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Validasi kepemilikan bisa ditambahkan di sini jika perlu

        kategori.setNama(request.getNama());
        kategori.setKeterangan(request.getKeterangan());
        kategori.setNominal(request.getNominal() != null ? request.getNominal() : BigDecimal.ZERO);

        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    // Update Status Aktif/Nonaktif
    public KategoriDto updateStatusKategori(Long id, Boolean status) {
        Kategori k = kategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        k.setAktif(status);
        kategoriRepository.save(k);
        return toDto(k);
    }

    public void deleteKategori(Long id) {
        kategoriRepository.deleteById(id);
    }

    // ==================================================================================
    // 5. CRUD KELAS & ANGKATAN (Standard)
    // ==================================================================================

    // --- KELAS ---
    public KelasDto createKelas(KelasRequest request) {
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65)
                .orElseThrow(() -> new RuntimeException("Angkatan default tidak ditemukan"));

        Kelas kelas = Kelas.builder()
                .kode(request.getKode())
                .nama(request.getNama())
                .angkatan(angkatan)
                .build();

        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    public List<KelasDto> getAllKelas() {
        return kelasRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<KelasDto> getKelasByAngkatan(Long angkatanId) {
        return kelasRepository.findByAngkatanId(angkatanId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public KelasDto updateKelas(Long id, KelasRequest request) {
        Kelas kelas = kelasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kelas tidak ditemukan"));
        kelas.setKode(request.getKode());
        kelas.setNama(request.getNama());
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    public void deleteKelas(Long id) {
        kelasRepository.deleteById(id);
    }

    // --- ANGKATAN ---
    public AngkatanDto createAngkatan(AngkatanRequest request) {
        Angkatan angkatan = Angkatan.builder()
                .tahun(request.getTahun())
                .nama(request.getNama())
                .build();
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    public List<AngkatanDto> getAllAngkatan() {
        return angkatanRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public AngkatanDto updateAngkatan(Long id, AngkatanRequest request) {
        Angkatan angkatan = angkatanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Angkatan tidak ditemukan"));
        angkatan.setTahun(request.getTahun());
        angkatan.setNama(request.getNama());
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    public void deleteAngkatan(Long id) {
        angkatanRepository.deleteById(id);
    }

    // ==================================================================================
    // 6. HELPER METHODS
    // ==================================================================================

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByNim(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User login tidak ditemukan"));
    }

    private KategoriDto toDto(Kategori k) {
        // Hitung total uang terkumpul untuk kategori ini
        BigDecimal totalTerkumpul = transaksiRepository.sumTotalValidByKategori(k.getId());
        if (totalTerkumpul == null) totalTerkumpul = BigDecimal.ZERO;

        return KategoriDto.builder()
                .id(k.getId())
                .nama(k.getNama())
                .keterangan(k.getKeterangan())
                .level(k.getLevel())
                .nominal(k.getNominal() != null ? k.getNominal() : BigDecimal.ZERO)
                .totalTerkumpul(totalTerkumpul)
                .aktif(k.getAktif() != null ? k.getAktif() : true) // Sertakan status aktif
                .build();
    }

    private KelasDto toDto(Kelas k) {
        return KelasDto.builder()
                .id(k.getId())
                .kode(k.getKode())
                .nama(k.getNama())
                .build();
    }

    private AngkatanDto toDto(Angkatan a) {
        return AngkatanDto.builder()
                .id(a.getId())
                .tahun(a.getTahun())
                .nama(a.getNama())
                .build();
    }
}