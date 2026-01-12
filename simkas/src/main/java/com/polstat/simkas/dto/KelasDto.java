// File: src/main/java/com/polstat/simkas/dto/KelasDto.java
package com.polstat.simkas.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO untuk respons data Kelas
 * (Bagian dari Fitur Manajemen Data Master)
 */
@Data
@Builder
public class KelasDto {
    private Long id;
    private String kode;
    private String nama;
    private Long angkatanId;      // camelCase
    private String namaAngkatan;  // camelCase
}