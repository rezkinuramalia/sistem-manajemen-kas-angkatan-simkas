package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KategoriRepository extends JpaRepository<Kategori, Long> {

    // Query Manual: Cari semua wadah level ANGKATAN (Milik Admin)
    @Query("SELECT k FROM Kategori k WHERE k.level = 'ANGKATAN'")
    List<Kategori> findAllAngkatan();

    // Query Manual: Cari wadah level KELAS milik ID Kelas tertentu (Milik Bendahara)
    // Ini memperbaiki masalah list kosong karena salah baca nama kolom
    @Query("SELECT k FROM Kategori k WHERE k.level = 'KELAS' AND k.idKelasPemilik = :idKelas")
    List<Kategori> findByKelasMilik(@Param("idKelas") Long idKelas);
}