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

                        // 2. Master Data (Kategori)
                        .requestMatchers(HttpMethod.GET, "/api/master/kategori/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS", "ANGGOTA")
                        .requestMatchers(HttpMethod.POST, "/api/master/kategori/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS")
                        .requestMatchers(HttpMethod.PUT, "/api/master/kategori/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS")
                        .requestMatchers(HttpMethod.DELETE, "/api/master/kategori/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS")

                        // 3. TRANSAKSI (PERBAIKAN DISINI)
                        // Izinkan POST ke /api/transaksi (tanpa /**) untuk submit form
                        .requestMatchers(HttpMethod.POST, "/api/transaksi").hasAnyAuthority("ANGGOTA", "BENDAHARA_KELAS", "ADMIN_ANGKATAN")

                        // Izinkan endpoint transaksi lainnya (history, validasi, dll)
                        .requestMatchers("/api/transaksi/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS", "ANGGOTA")

                        // 4. File Uploads (Agar gambar bisa diakses/didownload jika perlu)
                        .requestMatchers("/uploads/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtConfig, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}