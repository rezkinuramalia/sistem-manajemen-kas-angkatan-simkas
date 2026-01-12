package com.polstat.simkas.controller;

import com.polstat.simkas.dto.UserDto;
import com.polstat.simkas.dto.UserProfileUpdateRequest;
import com.polstat.simkas.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for User Management (Profile, Edit, Change Pass)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Get Profile
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        UserDto userDto = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(userDto);
    }

    // 2. Update Profile
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateMyProfile(@RequestBody UserProfileUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        UserDto updatedUser = userService.updateUserProfile(username, request);
        return ResponseEntity.ok(updatedUser);
    }

    // 3. Change Password (THIS IS WHAT YOU NEED)
    // Using Map to capture oldPassword and newPassword directly
    @PutMapping("/password") // Changed to PUT to match REST standards better, but POST is fine too
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeMyPassword(@RequestBody Map<String, String> request, Authentication authentication) {
        String username = authentication.getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        try {
            // UserService handles the BCrypt logic
            userService.changeUserPassword(username, oldPassword, newPassword);
            return ResponseEntity.ok("Password successfully changed");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Delete Account
    @DeleteMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMyAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deactivateUserAccount(username);
        return ResponseEntity.ok("Account deactivated successfully");
    }
}