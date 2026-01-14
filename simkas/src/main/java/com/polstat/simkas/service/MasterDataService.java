package com.polstat.simkas.service;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    // Konstanta ID Angkatan 65 (Sesuai kode Anda sebelumnya)
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

    // =======================================================
    // 1. KATEGORI (WADAH KAS) - CORE LOGIC DIPERBAIKI
    // =======================================================

    // Custom Comparator: Yang AKTIF (true) di atas, lalu ID terbesar (terbaru) di atas
    private final Comparator<KategoriDto> kategoriSorter = (k1, k2) -> {
        // Handle null safety (anggap true jika null)
        boolean a1 = k1.getAktif() != null ? k1.getAktif() : true;
        boolean a2 = k2.getAktif() != null ? k2.getAktif() : true;

        // Jika status beda, yang aktif (true) duluan
        if (a1 != a2) {
            return Boolean.compare(a2, a1);
        }
        // Jika status sama, urutkan ID descending (terbaru diatas)
        return k2.getId().compareTo(k1.getId());
    };

    public KategoriDto createKategori(KategoriRequest request) {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        String level;
        Long idKelasPemilik = null;

        if ("BENDAHARA_KELAS".equals(role)) {
            level = "KELAS";
            if (user.getKelas() == null) throw new RuntimeException("Bendahara wajib memiliki Kelas!");
            idKelasPemilik = user.getKelas().getId(); // [PENTING] Simpan ID Kelas agar muncul di menu Kelola
        } else if ("ADMIN_ANGKATAN".equals(role)) {
            level = "ANGKATAN";
            idKelasPemilik = null; // Admin levelnya angkatan, tidak terikat kelas spesifik
        } else {
            throw new RuntimeException("Role tidak memiliki izin membuat wadah.");
        }

        Kategori kategori = Kategori.builder()
                .nama(request.getNama())
                .keterangan(request.getKeterangan())
                .level(level)
                .idKelasPemilik(idKelasPemilik)
                .nominal(request.getNominal() != null ? request.getNominal() : BigDecimal.ZERO)
                .aktif(true) // [PENTING] Default Aktif saat dibuat
                .build();

        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    public KategoriDto updateStatusKategori(Long id, Boolean status) {
        Kategori k = kategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        k.setAktif(status);
        kategoriRepository.save(k);
        return toDto(k);
    }

    // === LOGIKA TAMPILAN ANDROID ===

    // 1. Menu KELOLA (BERANDA): Menampilkan wadah yang DIBUAT/DIKELOLA User
    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriManagedByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        List<Kategori> rawList;

        if ("BENDAHARA_KELAS".equals(role)) {
            if (user.getKelas() == null) return Collections.emptyList();

            // [FIX] Menggunakan @Query manual 'findByKelasMilik'
            // Mencari wadah level 'KELAS' yang 'id_kelas_pemilik'-nya sama dengan user
            rawList = kategoriRepository.findByKelasMilik(user.getKelas().getId());

        } else if ("ADMIN_ANGKATAN".equals(role)) {
            // [FIX] Menggunakan @Query manual 'findAllAngkatan'
            // Mencari semua wadah level 'ANGKATAN'
            rawList = kategoriRepository.findAllAngkatan();
        } else {
            return Collections.emptyList();
        }

        List<KategoriDto> list = rawList.stream().map(this::toDto).collect(Collectors.toList());
        list.sort(kategoriSorter); // Urutkan: Aktif dulu, lalu Terbaru
        return list;
    }

    // 2. Menu SETOR (BAYAR): Menampilkan wadah tempat User HARUS BAYAR
    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriForPaymentByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        List<Kategori> rawList;

        if ("BENDAHARA_KELAS".equals(role)) {
            // Bendahara membayar ke wadah milik Admin (Level ANGKATAN)
            rawList = kategoriRepository.findAllAngkatan();
        } else {
            // Anggota/Mahasiswa membayar ke wadah milik Bendahara (Level KELAS)
            if (user.getKelas() == null) return Collections.emptyList();
            rawList = kategoriRepository.findByKelasMilik(user.getKelas().getId());
        }

        List<KategoriDto> list = rawList.stream().map(this::toDto).collect(Collectors.toList());
        list.sort(kategoriSorter);
        return list;
    }

    // Endpoint General (Opsional/Backup)
    @Transactional(readOnly = true)
    public List<KategoriDto> getAllKategoriSesuaiRole() {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        if ("ANGGOTA".equals(role)) {
            return getKategoriForPaymentByUser();
        } else if ("ADMIN_ANGKATAN".equals(role)) {
            return getKategoriManagedByUser();
        } else {
            // Bendahara: Gabungan Managed (untuk dikelola) + Payment (untuk dibayar)
            List<KategoriDto> managed = getKategoriManagedByUser();
            List<KategoriDto> payment = getKategoriForPaymentByUser();

            // Gabungkan dan hilangkan duplikat ID
            java.util.Set<Long> ids = new java.util.HashSet<>();
            List<KategoriDto> combined = new java.util.ArrayList<>();

            for (KategoriDto k : managed) { if (ids.add(k.getId())) combined.add(k); }
            for (KategoriDto k : payment) { if (ids.add(k.getId())) combined.add(k); }

            combined.sort(kategoriSorter);
            return combined;
        }
    }

    // Helper Update/Delete Kategori Biasa
    public KategoriDto updateKategori(Long id, KategoriRequest request) {
        Kategori kategori = kategoriRepository.findById(id).orElseThrow(() -> new RuntimeException("Kategori not found"));
        kategori.setNama(request.getNama());
        kategori.setKeterangan(request.getKeterangan());
        kategori.setNominal(request.getNominal() != null ? request.getNominal() : BigDecimal.ZERO);
        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    public void deleteKategori(Long id) { kategoriRepository.deleteById(id); }


    // =======================================================
    // 2. KELAS & ANGKATAN (MASTER DATA LAIN)
    // =======================================================

    public KelasDto createKelas(KelasRequest request) {
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65)
                .orElseThrow(() -> new RuntimeException("Angkatan not found"));
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
        // Ambil kelas hanya yang angkatan 65 (Opsional logic)
        return kelasRepository.findByAngkatanId(ID_ANGKATAN_65).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KelasDto> getKelasByAngkatan(Long angkatanId) {
        return kelasRepository.findByAngkatanId(angkatanId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public KelasDto updateKelas(Long id, KelasRequest request) {
        Kelas kelas = kelasRepository.findById(id).orElseThrow(() -> new RuntimeException("Kelas not found"));
        kelas.setKode(request.getKode());
        kelas.setNama(request.getNama());
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    public void deleteKelas(Long id) { kelasRepository.deleteById(id); }

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
                .map(this::toDto).collect(Collectors.toList());
    }

    public AngkatanDto updateAngkatan(Long id, AngkatanRequest request) {
        Angkatan angkatan = angkatanRepository.findById(id).orElseThrow(() -> new RuntimeException("Angkatan not found"));
        angkatan.setTahun(request.getTahun());
        angkatan.setNama(request.getNama());
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    public void deleteAngkatan(Long id) { angkatanRepository.deleteById(id); }


    // =======================================================
    // 3. PRIVATE HELPER METHODS (MAPPING & USER)
    // =======================================================

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByNim(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User login tidak ditemukan"));
    }

    // === [PERBAIKAN UTAMA: Mapping DTO] ===
    private KategoriDto toDto(Kategori k) {
        BigDecimal totalTerkumpul = transaksiRepository.sumTotalValidByKategori(k.getId());

        if (totalTerkumpul == null) totalTerkumpul = BigDecimal.ZERO;

        // [FIX] Pastikan field 'aktif' tidak null. Jika DB null, anggap true.
        boolean isAktif = k.getAktif() != null ? k.getAktif() : true;

        return KategoriDto.builder()
                .id(k.getId())
                .nama(k.getNama())
                .keterangan(k.getKeterangan())
                .level(k.getLevel())
                .nominal(k.getNominal() != null ? k.getNominal() : BigDecimal.ZERO)
                .totalTerkumpul(totalTerkumpul)
                .aktif(isAktif) // [PENTING] Mengisi field ini agar Android tidak "Nonaktif" terus
                .build();
    }

    private KelasDto toDto(Kelas k) {
        Long angkatanId = (k.getAngkatan() != null) ? k.getAngkatan().getId() : null;
        String namaAngkatan = (k.getAngkatan() != null) ? k.getAngkatan().getNama() : "-";

        return KelasDto.builder()
                .id(k.getId())
                .kode(k.getKode())
                .nama(k.getNama())
                .angkatanId(angkatanId)
                .namaAngkatan(namaAngkatan)
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