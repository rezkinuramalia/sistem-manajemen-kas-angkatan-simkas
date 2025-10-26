// File: src/main/java/com/polstat/simkas/dto/DashboardAngkatanResponse.java
package com.polstat.simkas.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO untuk respons data dashboard level ANGKATAN
 * (Bagian dari Fitur Dashboard)
 */
@Data
@Builder
public class DashboardAngkatanResponse {
    private String namaAngkatan;
    private Long idAngkatan;
    private BigDecimal totalPemasukan;
    private BigDecimal totalPengeluaran;
    private BigDecimal saldoKas;
    private Long totalTransaksiPending;
    private Long totalAnggotaBelumBayarBulanIni;
    private Integer jumlahKelas;
}