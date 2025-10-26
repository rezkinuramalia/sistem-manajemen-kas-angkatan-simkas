package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transaksi",
        indexes = {
                @Index(name = "idx_transaksi_user", columnList = "id_user"),
                @Index(name = "idx_transaksi_kelas", columnList = "id_kelas"),
                @Index(name = "idx_transaksi_angkatan", columnList = "id_angkatan")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // owner: mahasiswa yang pembayaran (mapped manually via id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // who input the record (bendahara or admin) - can be null if data migrated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_input_by")
    private User inputBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kelas", nullable = false)
    private Kelas kelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_angkatan", nullable = false)
    private Angkatan angkatan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kategori")
    private Kategori kategori;

    @Column(name = "bulan_kas", nullable = false)
    private Integer bulanKas; // 1..12

    @Column(name = "tahun_kas", nullable = false)
    private Integer tahunKas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal nominal;

    @Column(name = "tanggal_bayar", nullable = false)
    private Instant tanggalBayar = Instant.now();

    private String metodePembayaran;

    private String buktiUrl;

    @Column(columnDefinition = "TEXT")
    private String keterangan;

    // Jenis transaksi: PEMASUKAN atau PENGELUARAN
    @Column(name = "jenis_transaksi", nullable = false, columnDefinition = "VARCHAR(20) COMMENT 'Pilih antara PEMASUKAN atau PENGELUARAN'")
    private String jenisTransaksi;

    @Enumerated(EnumType.STRING)
    private StatusValidasi statusValidasi = StatusValidasi.VALID;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum StatusValidasi {
        PENDING, VALID, REJECTED
    }
}
