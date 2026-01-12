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

    // "ANGKATAN" (Dibuat Admin, untuk Bendahara bayar)
    // "KELAS" (Dibuat Bendahara, untuk Mahasiswa bayar)
    @Column(nullable = false)
    private String level;

    // Jika level="KELAS", ini diisi ID Kelas si Bendahara. Jika Admin, null.
    private Long idKelasPemilik;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
