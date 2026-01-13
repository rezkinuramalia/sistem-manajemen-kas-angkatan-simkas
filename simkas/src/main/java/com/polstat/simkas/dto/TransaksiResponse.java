package com.polstat.simkas.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransaksiResponse {
    // === Data Utama Transaksi ===
    private Long id;
    private BigDecimal nominal;
    private String keterangan;
    private String statusValidasi; // Contoh: PENDING, VALID, REJECTED
    private String jenisTransaksi; // Contoh: PEMASUKAN, PENGELUARAN
    private Instant tanggalBayar;

    // === Bukti Bayar (Penting untuk Android) ===
    private String buktiBayar;

    // === ID Relasi (Untuk referensi database) ===
    private Long idUser;
    private Long idInputBy;
    private Long idKelas;
    private Long idAngkatan;
    private Long idKategori;

    // === Detail Waktu Kas ===
    private Integer bulanKas;
    private Integer tahunKas;

    // === Data Tambahan (Optional/Untuk Tampilan Dashboard) ===
    // Field ini berguna jika Controller mengisinya agar frontend tidak perlu request ulang
    private String namaPengirim;
    private String nimPengirim;
    private String namaKelas;
    private String namaWadah;
}