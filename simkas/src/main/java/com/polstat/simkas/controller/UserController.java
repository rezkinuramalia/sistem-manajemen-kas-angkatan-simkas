// File: src/main/java/com/polstat/simkas/controller/UserController.java
package com.polstat.simkas.controller;

import com.polstat.simkas.dto.PasswordChangeRequest;
import com.polstat.simkas.dto.UserDto;
import com.polstat.simkas.dto.UserProfileUpdateRequest;
import com.polstat.simkas.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk Fitur 2: Manajemen Pengguna
 * (Profil, Edit, Ganti Pass, Hapus Akun)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Fitur: Get User Profile (GET)
     * Mendapatkan profil pengguna yang sedang login.
     * Mengembalikan UserDto yang berisi (nim, nama, email, phone, dll)
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        String username = authentication.getName(); // Get NIM from token
        UserDto userDto = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Fitur: Edit User Profile (PUT)
     * Memperbarui data profil (nama, email, phone) pengguna yang sedang login.
     * Request body TIDAK mengandung NIM.
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateMyProfile(@RequestBody UserProfileUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        UserDto updatedUser = userService.updateUserProfile(username, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Fitur: Ganti Password (POST)
     * Mengganti password pengguna yang sedang login.
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeMyPassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            userService.changeUserPassword(username, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Fitur: Hapus Akun (DELETE)
     * Menonaktifkan akun pengguna yang sedang login (Soft Delete).
     */
    @DeleteMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMyAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deactivateUserAccount(username);
        return ResponseEntity.ok("Account deactivated successfully");
    }
}