// File: src/main/java/com/polstat/simkas/dto/AngkatanDto.java
package com.polstat.simkas.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO untuk respons data Angkatan
 * (Bagian dari Fitur Manajemen Data Master)
 */
@Data
@Builder
public class AngkatanDto {
    private Long id;
    private Integer tahun;
    private String nama;
}