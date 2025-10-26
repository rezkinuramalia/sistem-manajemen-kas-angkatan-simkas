// File: src/main/java/com/polstat/simkas/dto/PasswordChangeRequest.java
package com.polstat.simkas.dto;

import lombok.Data;

/**
 * DTO untuk request body saat ganti password
 * (Bagian dari Fitur Manajemen Pengguna)
 */
@Data
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}