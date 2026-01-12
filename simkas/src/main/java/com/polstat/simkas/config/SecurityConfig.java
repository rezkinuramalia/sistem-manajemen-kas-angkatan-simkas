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
                        // Public Endpoints
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/uploads/**").permitAll()

                        // Transaksi (PENTING: Harus authenticated agar Mahasiswa bisa akses)
                        .requestMatchers("/api/transaksi/**").authenticated()

                        // Master Data (Kategori/Wadah)
                        .requestMatchers(HttpMethod.GET, "/api/master/**").authenticated() // Semua bisa lihat list
                        .requestMatchers(HttpMethod.POST, "/api/master/**").hasAnyAuthority("ADMIN_ANGKATAN", "BENDAHARA_KELAS") // Hanya admin/bendahara yg buat

                        // Dashboard
                        .requestMatchers("/api/dashboard/**").authenticated()

                        // User & Profile
                        .requestMatchers("/api/users/**").authenticated()

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