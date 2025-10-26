// File: src/main/java/com/polstat/simkas/dto/DashboardKelasResponse.java
package com.polstat.simkas.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO untuk respons data dashboard level KELAS
 * (Bagian dari Fitur Dashboard)
 */
@Data
@Builder
public class DashboardKelasResponse {
    private String namaKelas;
    private Long idKelas;
    private BigDecimal totalPemasukan;
    private BigDecimal totalPengeluaran;
    private BigDecimal saldoKas;
    private Long transaksiPending;
    private Long anggotaBelumBayarBulanIni;
}