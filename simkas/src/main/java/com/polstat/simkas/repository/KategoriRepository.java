package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KategoriRepository extends JpaRepository<Kategori, Long> {

    // [SOLUSI AMPUH] Gunakan nativeQuery = true (SQL Murni)
    // Ini memaksa sistem membaca langsung tabel 'kategori' tanpa mapping rumit
    @Query(value = "SELECT * FROM kategori WHERE level = 'ANGKATAN'", nativeQuery = true)
    List<Kategori> findAllAngkatanNative();

    @Query(value = "SELECT * FROM kategori WHERE level = 'KELAS' AND id_kelas_pemilik = :idKelas", nativeQuery = true)
    List<Kategori> findByKelasMilikNative(@Param("idKelas") Long idKelas);
}