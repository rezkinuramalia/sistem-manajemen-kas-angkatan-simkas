package com.polstat.simkas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO untuk create dan update transaksi.
 * Digunakan oleh ADMIN_ANGKATAN dan BENDAHARA_KELAS.
 * - BENDAHARA_KELAS hanya bisa buat/ubah transaksi untuk kelasnya sendiri.
 * - ADMIN_ANGKATAN bisa akses semua kelas.
 */
@Data
public class TransaksiRequest {
    private Long id;           // dipakai saat update transaksi
    private Long idUser;       // user yang bayar (mahasiswa)
    private Long idKelas;      // kelas transaksi (bendahara kelas otomatis dari kelasnya)
    private Long idAngkatan;
    private Long idKategori;

    private Integer bulanKas;  // 1..12
    private Integer tahunKas;

    private BigDecimal nominal;
    private String keterangan;
    private String metodePembayaran;

    // Tambahan untuk update
    private Instant tanggalBayar;       // bisa diubah bendahara kelas & admin angkatan
    private String statusValidasi;      // PENDING, VALID, REJECTED

    // Tambahan baru: jenis transaksi PEMASUKAN / PENGELUARAN
    private String jenisTransaksi;
}
