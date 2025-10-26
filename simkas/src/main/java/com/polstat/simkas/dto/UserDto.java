package com.polstat.simkas.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String nim;
    private String nama;
    private String email;
    private String phone;
    private String roleName;
    private Long kelasId;
    private Long angkatanId;
    private Boolean aktif;
}
