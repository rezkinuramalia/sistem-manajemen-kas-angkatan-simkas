package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface KategoriRepository extends JpaRepository<Kategori, Long> {

    // Gunakan JPQL eksplisit agar Spring tidak salah tafsir filter
    @Query("SELECT k FROM Kategori k WHERE k.level = :level")
    List<Kategori> findByLevel(@Param("level") String level);

    @Query("SELECT k FROM Kategori k WHERE k.level = :level AND k.idKelasPemilik = :idKelasPemilik")
    List<Kategori> findByLevelAndIdKelasPemilik(@Param("level") String level, @Param("idKelasPemilik") Long idKelasPemilik);
}