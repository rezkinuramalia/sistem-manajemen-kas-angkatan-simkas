// File: src/main/java/com/polstat/simkas/dto/KategoriRequest.java
package com.polstat.simkas.dto;

import lombok.Data;

/**
 * DTO untuk request body C/U Kategori
 * (Bagian dari Fitur Manajemen Data Master)
 */
@Data
public class KategoriRequest {
    private String nama;
    private String keterangan;
}