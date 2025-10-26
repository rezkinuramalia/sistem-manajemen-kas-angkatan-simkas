package com.polstat.simkas.controller;

import com.polstat.simkas.dto.AuthRequest;
import com.polstat.simkas.dto.AuthResponse;
import com.polstat.simkas.dto.RegisterRequest;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.service.MyUserDetailsService;
import com.polstat.simkas.service.UserService;
import com.polstat.simkas.util.JwtUtil;
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
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        UserDetails ud = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(ud.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, ud.getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // create User entity and save
        User u = User.builder()
                .nim(req.getNim())
                .nama(req.getNama())
                .email(req.getEmail())
                .password(req.getPassword())
                .build();
        User saved = userService.createUser(u, req.getRoleId(), req.getKelasId(), req.getAngkatanId());
        return ResponseEntity.ok("User created with id: " + saved.getId());
    }
}
