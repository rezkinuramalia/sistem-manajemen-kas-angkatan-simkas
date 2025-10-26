// File: src/main/java/com/polstat/simkas/controller/DashboardController.java
package com.polstat.simkas.controller;

import com.polstat.simkas.dto.DashboardAngkatanResponse;
import com.polstat.simkas.dto.DashboardKelasResponse;
import com.polstat.simkas.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller untuk Fitur Dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Endpoint untuk dashboard Bendahara Kelas
     */
    @GetMapping("/kelas")
    @PreAuthorize("hasAuthority('BENDAHARA_KELAS')")
    public ResponseEntity<DashboardKelasResponse> getDashboardKelas(Authentication authentication) {
        String username = authentication.getName();
        DashboardKelasResponse response = dashboardService.getDashboardKelas(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint untuk dashboard Admin Angkatan
     */
    @GetMapping("/angkatan")
    @PreAuthorize("hasAuthority('ADMIN_ANGKATAN')")
    public ResponseEntity<DashboardAngkatanResponse> getDashboardAngkatan(Authentication authentication) {
        String username = authentication.getName();
        DashboardAngkatanResponse response = dashboardService.getDashboardAngkatan(username);
        return ResponseEntity.ok(response);
    }
}