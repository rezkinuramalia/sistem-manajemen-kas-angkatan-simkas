package com.polstat.simkas.repository;

import com.polstat.simkas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNim(String nim);
    Optional<User> findByEmail(String email);
}
