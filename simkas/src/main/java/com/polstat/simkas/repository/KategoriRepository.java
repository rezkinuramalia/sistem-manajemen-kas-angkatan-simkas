package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KategoriRepository extends JpaRepository<Kategori, Long> {
    // Cari berdasarkan Level (misal: ANGKATAN)
    List<Kategori> findByLevel(String level);

    // [TAMBAHAN WAJIB] Cari berdasarkan Level DAN Pemilik Kelas (untuk Bendahara)
    List<Kategori> findByLevelAndIdKelasPemilik(String level, Long idKelasPemilik);
}