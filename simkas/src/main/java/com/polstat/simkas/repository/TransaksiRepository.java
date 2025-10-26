// File: src/main/java/com/polstat/simkas/repository/TransaksiRepository.java
// GANTI SELURUH ISI FILE INI
package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Transaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    List<Transaksi> findByUserId(Long userId);
    List<Transaksi> findByKelasId(Long kelasId);
    List<Transaksi> findByAngkatanId(Long angkatanId);
    List<Transaksi> findByAngkatanIdAndKelasId(Long angkatanId, Long kelasId);

    // === TAMBAHAN UNTUK DASHBOARD ===
    @Query("SELECT COALESCE(SUM(t.nominal), 0) FROM Transaksi t WHERE t.kelas.id = :kelasId AND t.jenisTransaksi = :jenisTransaksi AND t.statusValidasi = 'VALID'")
    BigDecimal sumNominalByKelasAndJenis(@Param("kelasId") Long kelasId, @Param("jenisTransaksi") String jenisTransaksi);

    @Query("SELECT COALESCE(SUM(t.nominal), 0) FROM Transaksi t WHERE t.angkatan.id = :angkatanId AND t.jenisTransaksi = :jenisTransaksi AND t.statusValidasi = 'VALID'")
    BigDecimal sumNominalByAngkatanAndJenis(@Param("angkatanId") Long angkatanId, @Param("jenisTransaksi") String jenisTransaksi);

    @Query("SELECT COUNT(t) FROM Transaksi t WHERE t.kelas.id = :kelasId AND t.statusValidasi = 'PENDING'")
    Long countPendingByKelas(@Param("kelasId") Long kelasId);

    @Query("SELECT COUNT(t) FROM Transaksi t WHERE t.angkatan.id = :angkatanId AND t.statusValidasi = 'PENDING'")
    Long countPendingByAngkatan(@Param("angkatanId") Long angkatanId);
}