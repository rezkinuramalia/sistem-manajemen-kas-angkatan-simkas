package com.polstat.simkas.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nim;
    private String nama;
    private String email;
    private String password;
    private Long roleId;      // 1=ADMIN_ANGKAT,2=BENDAHARA_KELAS,3=ANGGOTA
    private Long kelasId;     // optional
    private Long angkatanId;  // optional
}
