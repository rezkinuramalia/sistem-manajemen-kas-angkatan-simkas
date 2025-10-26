// File: src/main/java/com/polstat/simkas/config/SecurityConfig.java
// GANTI SELURUH ISI FILE INI
package com.polstat.simkas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

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
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/users/**", "/api/dashboard/**", "/api/master/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/transaksi/angkatan/kelas").hasAuthority("ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.GET, "/api/transaksi/me/**").hasAnyAuthority("ANGGOTA", "BENDAHARA_KELAS", "ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.GET, "/api/transaksi/laporan/kelas").hasAuthority("BENDAHARA_KELAS")
                        .requestMatchers(HttpMethod.GET, "/api/transaksi/laporan/kelas/admin", "/api/transaksi/laporan/angkatan").hasAuthority("ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.GET, "/api/transaksi/laporan/user").hasAnyAuthority("BENDAHARA_KELAS", "ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.PUT, "/api/transaksi/validate/**").hasAnyAuthority("BENDAHARA_KELAS", "ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.GET, "/api/transaksi/kelas/**").hasAnyAuthority("BENDAHARA_KELAS","ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.POST, "/api/transaksi/kelas/**").hasAuthority("BENDAHARA_KELAS")
                        .requestMatchers(HttpMethod.PUT, "/api/transaksi/kelas/**").hasAnyAuthority("BENDAHARA_KELAS","ADMIN_ANGKATAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/transaksi/kelas/**").hasAnyAuthority("BENDAHARA_KELAS","ADMIN_ANGKATAN")
                        .requestMatchers("/api/transaksi/**").hasAuthority("ADMIN_ANGKATAN")
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