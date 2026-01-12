package com.polstat.simkas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransaksiResponse {
    private Long id;
    private Long idUser;
    private Long idInputBy;
    private Long idKelas;
    private Long idAngkatan;
    private Long idKategori;
    private Integer bulanKas;
    private Integer tahunKas;
    private BigDecimal nominal;
    private Instant tanggalBayar;
    private String keterangan;
    private String statusValidasi;
    private String jenisTransaksi;

    // === TAMBAHAN ===
    // Field ini wajib ada agar Android bisa mengambil nama file gambar dari API
    private String buktiBayar;
}