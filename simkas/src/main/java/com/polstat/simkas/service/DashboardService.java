// File: src/main/java/com/polstat/simkas/service/DashboardService.java
package com.polstat.simkas.service;

import com.polstat.simkas.dto.DashboardAngkatanResponse;
import com.polstat.simkas.dto.DashboardKelasResponse;
import com.polstat.simkas.entity.Angkatan;
import com.polstat.simkas.entity.Kelas;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.repository.KelasRepository;
import com.polstat.simkas.repository.StatusBulanRepository;
import com.polstat.simkas.repository.TransaksiRepository;
import com.polstat.simkas.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Service untuk logika bisnis Fitur Dashboard
 */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final TransaksiRepository transaksiRepository;
    private final StatusBulanRepository statusBulanRepository;
    private final KelasRepository kelasRepository;

    public DashboardService(UserRepository userRepository,
                            TransaksiRepository transaksiRepository,
                            StatusBulanRepository statusBulanRepository,
                            KelasRepository kelasRepository) {
        this.userRepository = userRepository;
        this.transaksiRepository = transaksiRepository;
        this.statusBulanRepository = statusBulanRepository;
        this.kelasRepository = kelasRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByNim(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));
    }

    /**
     * Logika untuk Dashboard BENDAHARA KELAS
     */
    public DashboardKelasResponse getDashboardKelas(String username) {
        User bendahara = getUserByUsername(username);
        Kelas kelas = bendahara.getKelas();
        if (kelas == null) {
            throw new RuntimeException("Bendahara tidak terdaftar di kelas manapun");
        }
        Long kelasId = kelas.getId();

        BigDecimal pemasukan = transaksiRepository.sumNominalByKelasAndJenis(kelasId, "PEMASUKAN");
        BigDecimal pengeluaran = transaksiRepository.sumNominalByKelasAndJenis(kelasId, "PENGELUARAN");
        BigDecimal saldo = pemasukan.subtract(pengeluaran);
        Long pending = transaksiRepository.countPendingByKelas(kelasId);

        YearMonth now = YearMonth.now();
        // Menghitung anggota AKTIF yang belum bayar
        Long belumBayar = statusBulanRepository.countUnpaidByKelasAndStatus(kelasId, now.getMonthValue(), now.getYear(), true);

        return DashboardKelasResponse.builder()
                .namaKelas(kelas.getNama())
                .idKelas(kelasId)
                .totalPemasukan(pemasukan)
                .totalPengeluaran(pengeluaran)
                .saldoKas(saldo)
                .transaksiPending(pending)
                .anggotaBelumBayarBulanIni(belumBayar)
                .build();
    }

    /**
     * Logika untuk Dashboard ADMIN ANGKATAN
     */
    public DashboardAngkatanResponse getDashboardAngkatan(String username) {
        User admin = getUserByUsername(username);
        Angkatan angkatan = admin.getAngkatan();
        if (angkatan == null) {
            throw new RuntimeException("Admin tidak terdaftar di angkatan manapun");
        }
        Long angkatanId = angkatan.getId();

        BigDecimal pemasukan = transaksiRepository.sumNominalByAngkatanAndJenis(angkatanId, "PEMASUKAN");
        BigDecimal pengeluaran = transaksiRepository.sumNominalByAngkatanAndJenis(angkatanId, "PENGELUARAN");
        BigDecimal saldo = pemasukan.subtract(pengeluaran);
        Long pending = transaksiRepository.countPendingByAngkatan(angkatanId);
        Integer jumlahKelas = kelasRepository.findByAngkatanId(angkatanId).size();

        YearMonth now = YearMonth.now();
        // Menghitung anggota AKTIF yang belum bayar
        Long belumBayar = statusBulanRepository.countUnpaidByAngkatanAndStatus(angkatanId, now.getMonthValue(), now.getYear(), true);

        return DashboardAngkatanResponse.builder()
                .namaAngkatan(angkatan.getNama())
                .idAngkatan(angkatanId)
                .totalPemasukan(pemasukan)
                .totalPengeluaran(pengeluaran)
                .saldoKas(saldo)
                .totalTransaksiPending(pending)
                .totalAnggotaBelumBayarBulanIni(belumBayar)
                .jumlahKelas(jumlahKelas)
                .build();
    }
}