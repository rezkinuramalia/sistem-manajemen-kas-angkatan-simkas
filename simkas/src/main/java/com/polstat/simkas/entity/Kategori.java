package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "kategori")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kategori {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    private String keterangan;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
