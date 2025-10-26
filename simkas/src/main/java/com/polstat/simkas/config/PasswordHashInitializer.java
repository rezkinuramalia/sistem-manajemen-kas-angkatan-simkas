package com.polstat.simkas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.polstat.simkas.repository.UserRepository;
import com.polstat.simkas.entity.User;

@Component
public class PasswordHashInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordHashInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        userRepository.findAll().forEach(user -> {
            String pwd = user.getPassword();

            // cek kalau belum di-hash
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                System.out.println("✅ Password user dengan email " + user.getEmail() + " sudah di-hash!");
            }
        });
    }
}
