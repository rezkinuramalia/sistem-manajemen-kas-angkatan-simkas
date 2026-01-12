package com.polstat.simkas.repository;
import com.polstat.simkas.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KategoriRepository extends JpaRepository<Kategori, Long> {
    List<Kategori> findByLevel(String level);
    List<Kategori> findByLevelAndIdKelasPemilik(String level, Long idKelasPemilik);
}