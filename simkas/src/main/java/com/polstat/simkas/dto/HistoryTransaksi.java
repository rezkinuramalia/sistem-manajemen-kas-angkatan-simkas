package com.polstat.simkas.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class HistoryTransaksi {
    private Long id;
    private BigDecimal nominal;
    private String keterangan;
    private String statusValidasi; // PENDING, VALID, REJECTED
    private String tanggalBayar;   // Kita kirim sebagai String biar Android mudah bacanya
    private String namaWadah;      // Nama Kategori/Tempat Bayar
}