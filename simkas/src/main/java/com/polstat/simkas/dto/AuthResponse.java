package com.polstat.simkas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Long id;  // ✅ PERBAIKAN: Tambahkan user ID
    private String role;  // ✅ PERBAIKAN: Tambahkan role
}

