// File: src/main/java/com/polstat/simkas/dto/UserProfileUpdateRequest.java
package com.polstat.simkas.dto;

import lombok.Data;

/**
 * DTO untuk request body saat edit profil
 * (Bagian dari Fitur Manajemen Pengguna)
 */
@Data
public class UserProfileUpdateRequest {
    private String nama;
    private String email;
    private String phone;
    private Long kelasId;
    private Long angkatanId;
}