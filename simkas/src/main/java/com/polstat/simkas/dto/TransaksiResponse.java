package com.polstat.simkas.dto;

import lombok.Data;
import java.math.BigDecimal;
// Hapus import Instant, ganti String untuk tanggalBayar agar kompatibel dengan Android
// import java.time.Instant;

@Data
public class TransaksiResponse {
    // === Data Utama Transaksi ===
    private Long id;
    private BigDecimal nominal;
    private String keterangan;
    private String statusValidasi;
    private String jenisTransaksi;

    // [PERBAIKAN] Ubah Instant ke String agar Android bisa membacanya tanpa error parsing
    private String tanggalBayar;

    private String buktiBayar;
    private String catatanAdmin;

    // === ID Relasi ===
    private Long idUser;
    private Long idInputBy;
    private Long idKelas;
    private Long idAngkatan;
    private Long idKategori;

    // === Detail Waktu Kas ===
    private Integer bulanKas;
    private Integer tahunKas;

    // === Data Tambahan ===
    private String namaPengirim;
    private String nimPengirim;
    private String namaKelas;
    private String namaWadah;
}