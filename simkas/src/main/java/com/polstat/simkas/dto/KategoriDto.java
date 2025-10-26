// File: src/main/java/com/polstat/simkas/dto/KategoriDto.java
package com.polstat.simkas.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO untuk respons data Kategori
 * (Bagian dari Fitur Manajemen Data Master)
 */
@Data
@Builder
public class KategoriDto {
    private Long id;
    private String nama;
    private String keterangan;
}