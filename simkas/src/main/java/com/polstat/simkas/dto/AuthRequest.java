package com.polstat.simkas.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username; // kita gunakan nim atau email sebagai username
    private String password;
}
