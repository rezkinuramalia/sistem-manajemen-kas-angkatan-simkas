package com.polstat.simkas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtConfig jwtConfig;

    public SecurityConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Auth public
                        .requestMatchers("/api/auth/**").permitAll()

                        // --- TAMBAHAN: Izinkan akses public untuk list kelas & angkatan (Buat Register) ---
                        .requestMatchers(HttpMethod.GET, "/api/master/kelas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/master/angkatan").permitAll()
                        // -------------------------------------------------------------------------------

                        // 2. Master Data (Sisanya butuh login)
                        .requestMatchers(HttpMethod.GET, "/api/master/kategori/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS", "ANGGOTA")
                        // ... (kode lainnya tetap sama)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtConfig, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}