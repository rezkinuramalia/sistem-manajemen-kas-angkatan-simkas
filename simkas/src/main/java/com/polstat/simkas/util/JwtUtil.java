package com.polstat.simkas.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "supersecretkeyforsimkasapplicationjwt123456"; // minimal 32 chars
    private static final long EXPIRATION_TIME = 86400000; // 24 jam

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(String username, String role) {
        // Buat Claims map untuk menyimpan data role
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", role); // MASUKKAN ROLE KEDALAM TOKEN

        return Jwts.builder()
                .setClaims(claims) // SET CLAIMS DISINI
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    // [OPSIONAL] Method untuk ekstrak role dari token (berguna buat debug)
    public String extractRole(String token) {
        return (String) parseClaims(token).getBody().get("role");
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
