// File: src/main/java/com/polstat/simkas/service/MasterDataService.java
package com.polstat.simkas.service;

import com.polstat.simkas.dto.*;
import com.polstat.simkas.entity.Angkatan;
import com.polstat.simkas.entity.Kategori;
import com.polstat.simkas.entity.Kelas;
import com.polstat.simkas.repository.AngkatanRepository;
import com.polstat.simkas.repository.KategoriRepository;
import com.polstat.simkas.repository.KelasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk logika bisnis Fitur Manajemen Data Master
 */
@Service
@Transactional // Set transaksi di level kelas
public class MasterDataService {

    private final KategoriRepository kategoriRepository;
    private final KelasRepository kelasRepository;
    private final AngkatanRepository angkatanRepository;

    public MasterDataService(KategoriRepository kategoriRepository, KelasRepository kelasRepository, AngkatanRepository angkatanRepository) {
        this.kategoriRepository = kategoriRepository;
        this.kelasRepository = kelasRepository;
        this.angkatanRepository = angkatanRepository;
    }

    // === KATEGORI ===
    public KategoriDto createKategori(KategoriRequest request) {
        Kategori kategori = Kategori.builder()
                .nama(request.getNama())
                .keterangan(request.getKeterangan())
                .build();
        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    @Transactional(readOnly = true)
    public List<KategoriDto> getAllKategori() {
        return kategoriRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public KategoriDto updateKategori(Long id, KategoriRequest request) {
        Kategori kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori not found"));
        kategori.setNama(request.getNama());
        kategori.setKeterangan(request.getKeterangan());
        kategoriRepository.save(kategori);
        return toDto(kategori);
    }

    public void deleteKategori(Long id) {
        // TODO: Tambahkan validasi apakah kategori sedang dipakai di tabel transaksi
        kategoriRepository.deleteById(id);
    }

    // === KELAS ===
    public KelasDto createKelas(KelasRequest request) {
        Angkatan angkatan = angkatanRepository.findById(request.getAngkatanId())
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
        return kelasRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KelasDto> getKelasByAngkatan(Long angkatanId) {
        return kelasRepository.findByAngkatanId(angkatanId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public KelasDto updateKelas(Long id, KelasRequest request) {
        Kelas kelas = kelasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kelas not found"));
        Angkatan angkatan = angkatanRepository.findById(request.getAngkatanId())
                .orElseThrow(() -> new RuntimeException("Angkatan not found"));

        kelas.setKode(request.getKode());
        kelas.setNama(request.getNama());
        kelas.setAngkatan(angkatan);
        kelasRepository.save(kelas);
        return toDto(kelas);
    }

    public void deleteKelas(Long id) {
        // TODO: Tambahkan validasi apakah ada user di kelas ini
        kelasRepository.deleteById(id);
    }

    // === ANGKATAN ===
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
        return angkatanRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
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
        // TODO: Tambahkan validasi apakah ada kelas di angkatan ini
        angkatanRepository.deleteById(id);
    }

    // === Helper DTO Converters ===
    private KategoriDto toDto(Kategori k) {
        return KategoriDto.builder().id(k.getId()).nama(k.getNama()).keterangan(k.getKeterangan()).build();
    }

    private KelasDto toDto(Kelas k) {
        return KelasDto.builder()
                .id(k.getId())
                .kode(k.getKode())
                .nama(k.getNama())
                .angkatanId(k.getAngkatan().getId())
                .namaAngkatan(k.getAngkatan().getTahun().toString()) // Asumsi nama angkatan = tahun
                .build();
    }

    private AngkatanDto toDto(Angkatan a) {
        return AngkatanDto.builder().id(a.getId()).tahun(a.getTahun()).nama(a.getNama()).build();
    }
}