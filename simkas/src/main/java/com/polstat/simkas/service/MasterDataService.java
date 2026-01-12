package com.polstat.simkas.service;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MasterDataService {

    private final KategoriRepository kategoriRepository;
    private final KelasRepository kelasRepository;
    private final AngkatanRepository angkatanRepository;
    private final UserRepository userRepository;

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
    // 1. KATEGORI (WADAH KAS)
    // =======================================================

    public KategoriDto createKategori(KategoriRequest request) {
        User user = getCurrentUser();
        String role = user.getRole().getName();
        String level;
        Long idKelasPemilik = null;

        System.out.println("DEBUG: Creating Kategori by " + user.getNama() + " Role: " + role);

        if ("BENDAHARA_KELAS".equals(role)) {
            level = "KELAS";
            if (user.getKelas() == null) {
                throw new RuntimeException("Bendahara tidak terdaftar di kelas manapun!");
            }
            idKelasPemilik = user.getKelas().getId();
            System.out.println("DEBUG: Assigned to Kelas ID: " + idKelasPemilik);
        } else if ("ADMIN_ANGKATAN".equals(role)) {
            level = "ANGKATAN";
            idKelasPemilik = null;
        } else {
            throw new RuntimeException("Role ini tidak diizinkan membuat wadah.");
        }

        Kategori kategori = Kategori.builder()
                .nama(request.getNama())
                .keterangan(request.getKeterangan())
                .level(level)
                .idKelasPemilik(idKelasPemilik)
                // Default nominal 0 jika null
                .nominal(request.getNominal() != null ? request.getNominal() : BigDecimal.ZERO)
                .build();

        kategoriRepository.save(kategori);
        System.out.println("DEBUG: Kategori Saved! ID: " + kategori.getId());
        return toDto(kategori);
    }

    // === LOGIKA TAMPILAN ANDROID ===

    @Transactional(readOnly = true)
    public List<KategoriDto> getAllKategoriSesuaiRole() {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        System.out.println("DEBUG: Fetching List for User: " + user.getNama() + " | Role: " + role);

        if ("ANGGOTA".equals(role)) {
            return getKategoriForPaymentByUser();
        } else {
            return getKategoriManagedByUser();
        }
    }

    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriManagedByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        if ("ADMIN_ANGKATAN".equals(role)) {
            return kategoriRepository.findByLevel("ANGKATAN")
                    .stream().map(this::toDto).collect(Collectors.toList());
        } else if ("BENDAHARA_KELAS".equals(role)) {
            if (user.getKelas() == null) {
                System.out.println("DEBUG: Bendahara has NO CLASS! List Empty.");
                return List.of();
            }
            System.out.println("DEBUG: Fetching Managed for Kelas ID: " + user.getKelas().getId());
            List<KategoriDto> list = kategoriRepository.findByLevelAndIdKelasPemilik("KELAS", user.getKelas().getId())
                    .stream().map(this::toDto).collect(Collectors.toList());
            System.out.println("DEBUG: Found " + list.size() + " categories.");
            return list;
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<KategoriDto> getKategoriForPaymentByUser() {
        User user = getCurrentUser();
        String role = user.getRole().getName();

        if ("BENDAHARA_KELAS".equals(role)) {
            return kategoriRepository.findByLevel("ANGKATAN")
                    .stream().map(this::toDto).collect(Collectors.toList());
        } else if ("ANGGOTA".equals(role)) {
            if (user.getKelas() == null) {
                System.out.println("DEBUG: Mahasiswa has NO CLASS! List Empty.");
                return List.of();
            }
            System.out.println("DEBUG: Fetching Payment for Kelas ID: " + user.getKelas().getId());
            List<KategoriDto> list = kategoriRepository.findByLevelAndIdKelasPemilik("KELAS", user.getKelas().getId())
                    .stream().map(this::toDto).collect(Collectors.toList());
            System.out.println("DEBUG: Found " + list.size() + " items to pay.");
            return list;
        }
        return List.of();
    }

    // Helper Methods standard (Update, Delete, etc)
    public KategoriDto updateKategori(Long id, KategoriRequest request) {
        Kategori kategori = kategoriRepository.findById(id).orElseThrow(() -> new RuntimeException("Kategori not found"));
        kategori.setNama(request.getNama());
        kategori.setKeterangan(request.getKeterangan());
        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    public void deleteKategori(Long id) { kategoriRepository.deleteById(id); }

    public KelasDto createKelas(KelasRequest request) {
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65).orElseThrow(() -> new RuntimeException("Angkatan not found"));
        Kelas kelas = Kelas.builder().kode(request.getKode()).nama(request.getNama()).angkatan(angkatan).build();
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    @Transactional(readOnly = true)
    public List<KelasDto> getAllKelas() {
        return kelasRepository.findByAngkatanId(ID_ANGKATAN_65).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KelasDto> getKelasByAngkatan(Long angkatanId) {
        return kelasRepository.findByAngkatanId(angkatanId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public KelasDto updateKelas(Long id, KelasRequest request) {
        Kelas kelas = kelasRepository.findById(id).orElseThrow(() -> new RuntimeException("Kelas not found"));
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65).orElseThrow(() -> new RuntimeException("Angkatan not found"));
        kelas.setKode(request.getKode());
        kelas.setNama(request.getNama());
        kelas.setAngkatan(angkatan);
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    public void deleteKelas(Long id) { kelasRepository.deleteById(id); }

    public AngkatanDto createAngkatan(AngkatanRequest request) {
        Angkatan angkatan = Angkatan.builder().tahun(request.getTahun()).nama(request.getNama()).build();
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    @Transactional(readOnly = true)
    public List<AngkatanDto> getAllAngkatan() {
        return angkatanRepository.findAll().stream().filter(a -> a.getId().equals(ID_ANGKATAN_65)).map(this::toDto).collect(Collectors.toList());
    }

    public AngkatanDto updateAngkatan(Long id, AngkatanRequest request) {
        Angkatan angkatan = angkatanRepository.findById(id).orElseThrow(() -> new RuntimeException("Angkatan not found"));
        angkatan.setTahun(request.getTahun());
        angkatan.setNama(request.getNama());
        angkatanRepository.save(angkatan);
        return toDto(angkatan);
    }

    public void deleteAngkatan(Long id) { angkatanRepository.deleteById(id); }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByNim(username).or(() -> userRepository.findByEmail(username)).orElseThrow(() -> new RuntimeException("User login tidak ditemukan"));
    }

    private KategoriDto toDto(Kategori k) {
        return KategoriDto.builder()
                .id(k.getId())
                .nama(k.getNama())
                .keterangan(k.getKeterangan())
                .level(k.getLevel())
                .nominal(k.getNominal() != null ? k.getNominal() : BigDecimal.ZERO)
                // PENTING: Kirim angka 0 untuk mencegah aplikasi Android blank
                .totalTerkumpul(BigDecimal.ZERO)
                .build();
    }

    private KelasDto toDto(Kelas k) {
        Long angkatanId = (k.getAngkatan() != null) ? k.getAngkatan().getId() : null;
        String namaAngkatan = (k.getAngkatan() != null && k.getAngkatan().getTahun() != null) ? k.getAngkatan().getTahun().toString() : "-";
        return KelasDto.builder().id(k.getId()).kode(k.getKode()).nama(k.getNama()).angkatanId(angkatanId).namaAngkatan(namaAngkatan).build();
    }

    private AngkatanDto toDto(Angkatan a) {
        return AngkatanDto.builder().id(a.getId()).tahun(a.getTahun()).nama(a.getNama()).build();
    }
}