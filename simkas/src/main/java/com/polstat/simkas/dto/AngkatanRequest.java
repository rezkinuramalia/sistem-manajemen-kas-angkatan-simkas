// File: src/main/java/com/polstat/simkas/dto/AngkatanRequest.java
package com.polstat.simkas.dto;

import lombok.Data;

/**
 * DTO untuk request body C/U Angkatan
 * (Bagian dari Fitur Manajemen Data Master)
 */
@Data
public class AngkatanRequest {
    private Integer tahun;
    private String nama;
}