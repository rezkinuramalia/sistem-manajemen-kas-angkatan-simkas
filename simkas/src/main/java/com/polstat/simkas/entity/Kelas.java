package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kelas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kelas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String kode; // 3SI1, 3SI2

    private String nama;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "angkatan_id", nullable = false)
    private Angkatan angkatan;

    @Column(name = "created_at", updatable = false)
    private java.time.Instant createdAt = java.time.Instant.now();
}
