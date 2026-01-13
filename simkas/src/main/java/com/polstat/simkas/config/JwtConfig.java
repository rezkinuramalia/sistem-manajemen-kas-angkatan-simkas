package com.polstat.simkas.config;

import com.polstat.simkas.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtConfig extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        final String role;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            username = jwtUtil.extractUsername(jwt);
            role = jwtUtil.extractRole(jwt); // Pastikan ini mengembalikan String (misal: "BENDAHARA_KELAS")

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(jwt)) {

                    // PERBAIKAN: Pastikan role tidak null sebelum membuat Authority
                    // Gunakan nama role mentah karena di SecurityConfig Anda pakai hasAnyAuthority('BENDAHARA_KELAS')
                    // Cari baris yang membuat authorities, ganti dengan ini:
                    List<SimpleGrantedAuthority> authorities = (role != null && !role.isEmpty())
                            ? List.of(new SimpleGrantedAuthority(role)) // Sesuai dengan hasAnyAuthority di SecurityConfig
                            : Collections.emptyList();

// Debugging: Tambahkan baris ini untuk melihat apakah role berhasil dibaca di Terminal IntelliJ
                    System.out.println("🔍 JWT Check -> User: " + username + " | Authority: " + authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error JWT Auth: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}