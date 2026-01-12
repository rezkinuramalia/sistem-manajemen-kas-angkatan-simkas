package com.polstat.simkas;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TesPassword {
    public static void main(String[] args) {
        // 1. Inisialisasi Encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 2. Password yang mau kamu pakai
        String passwordMentah = "melody123";

        // 3. Generate Hash
        String passwordHash = encoder.encode(passwordMentah);

        // 4. Tampilkan Hasil
        System.out.println("=========================================");
        System.out.println("PASSWORD ASLI : " + passwordMentah);
        System.out.println("HASH BCRYPT   : " + passwordHash);
        System.out.println("=========================================");
    }
}