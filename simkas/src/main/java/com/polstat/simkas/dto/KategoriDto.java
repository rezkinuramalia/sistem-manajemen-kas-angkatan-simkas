package com.polstat.simkas.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class KategoriDto {
    private Long id;
    private String nama;
    private String keterangan;
    private String level;
    private BigDecimal nominal;  // ✅ PERBAIKAN: Tambahkan nominal pembayaran
    private BigDecimal totalTerkumpul; // Total dana yang sudah terkumpul untuk wadah ini
}