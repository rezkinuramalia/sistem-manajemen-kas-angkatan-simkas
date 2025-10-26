// File: src/main/java/com/polstat/simkas/repository/StatusBulanRepository.java
// GANTI SELURUH ISI FILE INI
package com.polstat.simkas.repository;

import com.polstat.simkas.entity.StatusBulan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StatusBulanRepository extends JpaRepository<StatusBulan, Long> {
    Optional<StatusBulan> findByUserIdAndBulanAndTahun(Long userId, Integer bulan, Integer tahun);

    // === TAMBAHAN UNTUK DASHBOARD ===
    @Query("SELECT COUNT(s) FROM StatusBulan s JOIN s.user u WHERE s.kelas.id = :kelasId AND s.bulan = :bulan AND s.tahun = :tahun AND s.isPaid = false AND u.aktif = :isAktif")
    Long countUnpaidByKelasAndStatus(@Param("kelasId") Long kelasId, @Param("bulan") Integer bulan, @Param("tahun") Integer tahun, @Param("isAktif") Boolean isAktif);

    @Query("SELECT COUNT(s) FROM StatusBulan s JOIN s.user u WHERE s.angkatan.id = :angkatanId AND s.bulan = :bulan AND s.tahun = :tahun AND s.isPaid = false AND u.aktif = :isAktif")
    Long countUnpaidByAngkatanAndStatus(@Param("angkatanId") Long angkatanId, @Param("bulan") Integer bulan, @Param("tahun") Integer tahun, @Param("isAktif") Boolean isAktif);
}