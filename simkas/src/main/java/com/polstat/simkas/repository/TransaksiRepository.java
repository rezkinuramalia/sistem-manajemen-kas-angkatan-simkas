// File: src/main/java/com/polstat/simkas/repository/TransaksiRepository.java
package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Transaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {

    // ==========================================
    // 1. BASIC FINDERS
    // ==========================================
    List<Transaksi> findByUserId(Long userId);

    // [PENTING] Ini untuk Fitur History di Android (Urut dari terbaru)
    List<Transaksi> findByUserIdOrderByTanggalBayarDesc(Long userId);

    List<Transaksi> findByKelasId(Long kelasId);
    List<Transaksi> findByAngkatanId(Long angkatanId);
    List<Transaksi> findByAngkatanIdAndKelasId(Long angkatanId, Long kelasId);

    // ==========================================
    // 2. LOGIKA BERJENJANG (Hierarki Role)
    // ==========================================

    // Untuk BENDAHARA KELAS: Hanya melihat transaksi dari ANGGOTA (Mahasiswa) di kelasnya
    @Query("SELECT t FROM Transaksi t WHERE t.kelas.id = :kelasId AND t.user.role.name = 'ANGGOTA'")
    List<Transaksi> findMahasiswaTransactionsByKelas(@Param("kelasId") Long kelasId);

    // Untuk ADMIN ANGKATAN: Hanya melihat transaksi setoran dari BENDAHARA_KELAS
    @Query("SELECT t FROM Transaksi t WHERE t.angkatan.id = :angkatanId AND t.user.role.name = 'BENDAHARA_KELAS'")
    List<Transaksi> findBendaharaTransactionsByAngkatan(@Param("angkatanId") Long angkatanId);

    // ==========================================
    // 3. DASHBOARD GLOBAL (Total & Pending)
    // ==========================================

    // Hitung Total Nominal per Kelas (Hanya yang VALID)
    @Query("SELECT COALESCE(SUM(t.nominal), 0) FROM Transaksi t WHERE t.kelas.id = :kelasId AND t.jenisTransaksi = :jenisTransaksi AND t.statusValidasi = 'VALID'")
    BigDecimal sumNominalByKelasAndJenis(@Param("kelasId") Long kelasId, @Param("jenisTransaksi") String jenisTransaksi);

    // Hitung Total Nominal per Angkatan (Hanya yang VALID)
    @Query("SELECT COALESCE(SUM(t.nominal), 0) FROM Transaksi t WHERE t.angkatan.id = :angkatanId AND t.jenisTransaksi = :jenisTransaksi AND t.statusValidasi = 'VALID'")
    BigDecimal sumNominalByAngkatanAndJenis(@Param("angkatanId") Long angkatanId, @Param("jenisTransaksi") String jenisTransaksi);

    // Hitung Jumlah Menunggu Validasi di Kelas
    @Query("SELECT COUNT(t) FROM Transaksi t WHERE t.kelas.id = :kelasId AND t.statusValidasi = 'PENDING'")
    Long countPendingByKelas(@Param("kelasId") Long kelasId);

    // Hitung Jumlah Menunggu Validasi di Angkatan
    @Query("SELECT COUNT(t) FROM Transaksi t WHERE t.angkatan.id = :angkatanId AND t.statusValidasi = 'PENDING'")
    Long countPendingByAngkatan(@Param("angkatanId") Long angkatanId);

    // ==========================================
    // 4. DASHBOARD PER WADAH (KATEGORI)
    // ==========================================

    // Hitung total uang yang SUDAH VALID untuk satu Kategori (Tempat Kumpul) tertentu.
    @Query("SELECT COALESCE(SUM(t.nominal), 0) FROM Transaksi t WHERE t.kategori.id = :kategoriId AND t.statusValidasi = 'VALID'")
    BigDecimal sumTotalValidByKategori(@Param("kategoriId") Long kategoriId);
}