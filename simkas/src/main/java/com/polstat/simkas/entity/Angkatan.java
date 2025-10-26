package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "angkatan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Angkatan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer tahun;

    private String nama;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
