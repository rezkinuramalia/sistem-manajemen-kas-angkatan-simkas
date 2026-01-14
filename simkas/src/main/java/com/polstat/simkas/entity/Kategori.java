// File: simkas/src/main/java/com/polstat/simkas/entity/Kategori.java
package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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

    @Column(nullable = false)
    private String level;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal nominal = BigDecimal.ZERO;

    private Long idKelasPemilik;

    // Status Aktif/Nonaktif
    @Column(nullable = false)
    @Builder.Default
    private Boolean aktif = true;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}