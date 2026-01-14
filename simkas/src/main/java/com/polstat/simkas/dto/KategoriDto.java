// File: simkas/src/main/java/com/polstat/simkas/dto/KategoriDto.java
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
    private BigDecimal nominal;
    private BigDecimal totalTerkumpul;
    private Boolean aktif; // status tempat pembayaran
}