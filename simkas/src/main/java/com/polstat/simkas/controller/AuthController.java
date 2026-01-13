package com.polstat.simkas.controller;

import com.polstat.simkas.dto.AuthRequest;
import com.polstat.simkas.dto.AuthResponse;
import com.polstat.simkas.dto.RegisterRequest;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.service.MyUserDetailsService;
import com.polstat.simkas.service.UserService;
import com.polstat.simkas.util.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException; // Import ini penting
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final MyUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService,
                          MyUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            UserDetails ud = (UserDetails) auth.getPrincipal();
            User user = userService.getUserByUsername(ud.getUsername());
            String roleName = user.getRole() != null ? user.getRole().getName() : "ANGGOTA";

            String token = jwtUtil.generateToken(ud.getUsername(), roleName);

            return ResponseEntity.ok(new AuthResponse(token, ud.getUsername(), user.getId(), roleName));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login Gagal: Username atau Password salah.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            // Buat entity User
            User u = User.builder()
                    .nim(req.getNim())
                    .nama(req.getNama())
                    .email(req.getEmail())
                    .password(req.getPassword())
                    .build();

            // Simpan ke database
            User saved = userService.createUser(u, req.getRoleId(), req.getKelasId(), req.getAngkatanId());

            return ResponseEntity.ok("Registrasi Berhasil! ID User: " + saved.getId());

        } catch (DataIntegrityViolationException e) {
            // INI PERBAIKANNYA: Tangkap error duplicate entry
            return ResponseEntity.badRequest().body("Gagal: NIM atau Email sudah terdaftar. Silakan langsung Login.");
        } catch (Exception e) {
            // Tangkap error lain
            return ResponseEntity.status(500).body("Terjadi kesalahan sistem: " + e.getMessage());
        }
    }
}