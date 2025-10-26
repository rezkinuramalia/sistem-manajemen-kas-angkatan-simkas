package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "status_bulan", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_bulan_tahun", columnNames = {"id_user", "bulan", "tahun"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusBulan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kelas", nullable = false)
    private Kelas kelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_angkatan", nullable = false)
    private Angkatan angkatan;

    private Integer bulan; // 1..12

    private Integer tahun;

    private Boolean isPaid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_payment_id")
    private Transaksi lastPayment;

    private Instant lastPaymentAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
