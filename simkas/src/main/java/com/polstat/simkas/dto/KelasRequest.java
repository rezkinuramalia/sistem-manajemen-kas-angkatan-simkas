// File: src/main/java/com/polstat/simkas/dto/KelasRequest.java
package com.polstat.simkas.dto;

import lombok.Data;

/**
 * DTO untuk request body C/U Kelas
 * (Bagian dari Fitur Manajemen Data Master)
 */
@Data
public class KelasRequest {
    private String kode;
    private String nama;
    private Long angkatanId;
}