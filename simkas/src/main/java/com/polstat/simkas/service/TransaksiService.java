package com.polstat.simkas.service;

import com.polstat.simkas.dto.HistoryTransaksi; // <--- PENTING: Import ini
import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final UserRepository userRepository;
    private final KelasRepository kelasRepository;
    private final AngkatanRepository angkatanRepository;
    private final KategoriRepository kategoriRepository;
    private final StatusBulanRepository statusBulanRepository;
    private final ActivityLogRepository activityLogRepository;

    public TransaksiService(TransaksiRepository transaksiRepository,
                            UserRepository userRepository,
                            KelasRepository kelasRepository,
                            AngkatanRepository angkatanRepository,
                            KategoriRepository kategoriRepository,
                            StatusBulanRepository statusBulanRepository,
                            ActivityLogRepository activityLogRepository) {
        this.transaksiRepository = transaksiRepository;
        this.userRepository = userRepository;
        this.kelasRepository = kelasRepository;
        this.angkatanRepository = angkatanRepository;
        this.kategoriRepository = kategoriRepository;
        this.statusBulanRepository = statusBulanRepository;
        this.activityLogRepository = activityLogRepository;
    }

    // ========================
    // SUPPORT METHODS
    // ========================
    public User getUserByUsername(String username) {
        return userRepository.findByNim(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    public List<Transaksi> findByUserId(Long userId) {
        return transaksiRepository.findByUserId(userId);
    }

    public List<Transaksi> findByKelasUsername(String nim) {
        User user = getUserByUsername(nim);
        if (user.getKelas() == null) throw new RuntimeException("User tidak memiliki kelas");
        return transaksiRepository.findByKelasId(user.getKelas().getId());
    }

    public List<Transaksi> findByAngkatan(Long angkatanId, Long kelasId) {
        if (kelasId != null) {
            return transaksiRepository.findByAngkatanIdAndKelasId(angkatanId, kelasId);
        } else {
            return transaksiRepository.findByAngkatanId(angkatanId);
        }
    }

    private List<Transaksi> filterByBulanJenisTahun(List<Transaksi> list, Integer bulan, Integer tahun, String jenisTransaksi) {
        return list.stream()
                .filter(t -> (tahun == null || t.getTahunKas().equals(tahun)))
                .filter(t -> (bulan == null || t.getBulanKas().equals(bulan)))
                .filter(t -> (jenisTransaksi == null || t.getJenisTransaksi().equalsIgnoreCase(jenisTransaksi)))
                .collect(Collectors.toList());
    }

    // ========================
    // LOGIC DASHBOARD / LIST DATA (Sesuai Role Berjenjang)
    // ========================
    public List<Transaksi> findByUserIdAndActor(Long userId, User actor) {
        // 1. ADMIN ANGKATAN (Role ID 1)
        if (actor.getRole().getId() == 1) {
            if (actor.getAngkatan() == null) return new ArrayList<>();
            return transaksiRepository.findBendaharaTransactionsByAngkatan(actor.getAngkatan().getId());
        }
        // 2. BENDAHARA KELAS (Role ID 2)
        else if (actor.getRole().getId() == 2) {
            if (actor.getKelas() == null) return new ArrayList<>();
            return transaksiRepository.findMahasiswaTransactionsByKelas(actor.getKelas().getId());
        }
        // 3. ANGGOTA / MAHASISWA (Role ID 3)
        else {
            if (!actor.getId().equals(userId)) {
                throw new RuntimeException("Kamu tidak boleh lihat transaksi orang lain!");
            }
            return transaksiRepository.findByUserId(userId);
        }
    }

    // =================================================================
    // [BARU] GET HISTORY (Dipanggil Controller untuk Android)
    // =================================================================
    public List<HistoryTransaksi> getHistoryByUserId(Long userId) {
        // Ambil data urut dari tanggal bayar terbaru (Pastikan Repository sudah diupdate)
        List<Transaksi> list = transaksiRepository.findByUserIdOrderByTanggalBayarDesc(userId);

        // Convert Entity Transaksi -> DTO HistoryTransaksi
        return list.stream().map(t -> {
            String namaWadah = (t.getKategori() != null) ? t.getKategori().getNama() : "-";

            return HistoryTransaksi.builder()
                    .id(t.getId())
                    .nominal(t.getNominal())
                    .keterangan(t.getKeterangan())
                    .statusValidasi(t.getStatusValidasi() != null ? t.getStatusValidasi().name() : "PENDING")
                    .tanggalBayar(t.getTanggalBayar() != null ? t.getTanggalBayar().toString() : "-")
                    .namaWadah(namaWadah)
                    .build();
        }).collect(Collectors.toList());
    }

    // ========================
    // CREATE TRANSAKSI (Bendahara/Anggota -> Upload)
    // ========================
    @Transactional
    public Transaksi createTransaksiByRole(TransaksiRequest req, String nim) {
        User actor = getUserByUsername(nim);

        // Otomatis set kelas dari actor jika belum ada
        if (actor.getKelas() != null) {
            req.setIdKelas(actor.getKelas().getId());
        }

        return createTransaksiInternal(req, actor);
    }

    // ========================
    // CREATE TRANSAKSI (Admin Angkatan - Input Manual)
    // ========================
    @Transactional
    public Transaksi createTransaksiAdminAngkatan(TransaksiRequest req, String nim) {
        User admin = getUserByUsername(nim);

        if (!"ADMIN_ANGKATAN".equals(admin.getRole().getName())) {
            throw new RuntimeException("Hanya admin angkatan yang bisa pakai endpoint ini");
        }

        User payer = userRepository.findById(req.getIdUser())
                .orElseThrow(() -> new RuntimeException("User pembayar tidak ditemukan"));

        if (payer.getKelas() == null) {
            throw new RuntimeException("User pembayar belum memiliki kelas");
        }

        if (!payer.getKelas().getAngkatan().getId().equals(admin.getAngkatan().getId())) {
            throw new RuntimeException("User pembayar bukan berasal dari angkatan admin");
        }

        req.setIdKelas(payer.getKelas().getId());

        return createTransaksiInternal(req, admin);
    }

    // ========================
    // UPDATE, DELETE, VALIDATE
    // ========================
    @Transactional
    public Transaksi updateTransaksiByRole(Long id, TransaksiRequest req, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        // Validasi Akses
        if (actor.getRole().getId() == 2 && !transaksi.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa mengupdate transaksi untuk kelasnya sendiri");
        }
        if (actor.getRole().getId() == 1 && !transaksi.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
            throw new RuntimeException("Admin angkatan hanya bisa mengupdate transaksi untuk kelas di angkatannya sendiri");
        }

        if (req.getNominal() != null) transaksi.setNominal(req.getNominal());
        if (req.getKeterangan() != null) transaksi.setKeterangan(req.getKeterangan());
        if (req.getMetodePembayaran() != null) transaksi.setMetodePembayaran(req.getMetodePembayaran());
        if (req.getIdKategori() != null)
            kategoriRepository.findById(req.getIdKategori()).ifPresent(transaksi::setKategori);
        if (req.getTanggalBayar() != null) transaksi.setTanggalBayar(req.getTanggalBayar());
        if (req.getJenisTransaksi() != null) transaksi.setJenisTransaksi(req.getJenisTransaksi());

        transaksi.setInputBy(actor);
        Transaksi updated = transaksiRepository.save(transaksi);
        saveActivityLog(actor, "UPDATE_TRANSAKSI", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteTransaksiByRole(Long id, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        if (actor.getRole().getId() == 2 && !transaksi.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa menghapus transaksi untuk kelasnya sendiri");
        }
        if (actor.getRole().getId() == 1 && !transaksi.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
            throw new RuntimeException("Admin angkatan hanya bisa menghapus transaksi untuk kelas di angkatannya sendiri");
        }

        transaksiRepository.delete(transaksi);
        saveActivityLog(actor, "DELETE_TRANSAKSI", id);
    }

    @Transactional
    public Transaksi validateTransaksi(Long id, String status, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        if (actor.getRole().getId() == 2) { // Bendahara
            if (!transaksi.getKelas().getId().equals(actor.getKelas().getId())) {
                throw new RuntimeException("Bendahara hanya bisa memvalidasi transaksi kelasnya");
            }
        }

        transaksi.setStatusValidasi(Transaksi.StatusValidasi.valueOf(status.toUpperCase()));

        Transaksi updated = transaksiRepository.save(transaksi);
        saveActivityLog(actor, "VALIDATE_TRANSAKSI", updated.getId());
        return updated;
    }

    // ========================
    // LAPORAN & HELPERS
    // ========================
    public List<Transaksi> laporanKelas(String username, Integer bulan, Integer tahun, String jenisTransaksi,
                                        Long kelasId, boolean isAdmin) {
        User user = getUserByUsername(username);
        List<Transaksi> list;

        if (isAdmin) {
            if (kelasId != null) {
                list = findByAngkatan(user.getAngkatan().getId(), kelasId);
                if (list.isEmpty()) {
                    throw new RuntimeException("Kelas tidak ditemukan dalam angkatan admin");
                }
            } else {
                list = findByAngkatan(user.getAngkatan().getId(), null);
            }
        } else {
            list = findByKelasUsername(username);
        }

        return filterByBulanJenisTahun(list, bulan, tahun, jenisTransaksi);
    }

    public List<Transaksi> laporanUser(Long userId, Integer bulan, Integer tahun, String jenisTransaksi, String username) {
        User actor = getUserByUsername(username);
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (actor.getRole().getId() == 2 && !targetUser.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa lihat user sekelas");
        }

        List<Transaksi> list = findByUserId(userId);
        return filterByBulanJenisTahun(list, bulan, tahun, jenisTransaksi);
    }

    // ========================
    // INTERNAL CREATE (LOGIC SIMPAN)
    // ========================
    @Transactional
    private Transaksi createTransaksiInternal(TransaksiRequest req, User actor) {
        Transaksi t = new Transaksi();

        User payer = userRepository.findById(req.getIdUser())
                .orElseThrow(() -> new RuntimeException("User pembayar tidak ditemukan"));

        t.setUser(payer);
        t.setInputBy(actor);

        // Set Kelas
        if (req.getIdKelas() != null) {
            kelasRepository.findById(req.getIdKelas()).ifPresent(t::setKelas);
        } else if (payer.getKelas() != null) {
            t.setKelas(payer.getKelas());
        } else {
            throw new RuntimeException("Kelas harus ditentukan");
        }

        // Set Angkatan (Coba dari Req, kalau null ambil dari Payer)
        if (req.getIdAngkatan() != null) {
            angkatanRepository.findById(req.getIdAngkatan()).ifPresent(t::setAngkatan);
        } else if (payer.getKelas() != null && payer.getKelas().getAngkatan() != null) {
            t.setAngkatan(payer.getKelas().getAngkatan());
        }

        // Set Kategori / Wadah
        if (req.getIdKategori() != null) {
            kategoriRepository.findById(req.getIdKategori()).ifPresent(t::setKategori);
        }

        t.setBulanKas(req.getBulanKas());
        t.setTahunKas(req.getTahunKas());
        t.setNominal(req.getNominal());
        t.setKeterangan(req.getKeterangan());
        t.setMetodePembayaran("TRANSFER");
        t.setTanggalBayar(Instant.now());
        t.setStatusValidasi(Transaksi.StatusValidasi.PENDING);
        t.setJenisTransaksi(req.getJenisTransaksi());

        // [PENTING] Simpan Bukti Bayar
        t.setBuktiBayar(req.getBuktiBayar());

        Transaksi saved = transaksiRepository.save(t);

        // Update Status Bulan (Flag Paid)
        StatusBulan status = statusBulanRepository.findByUserIdAndBulanAndTahun(
                payer.getId(), req.getBulanKas(), req.getTahunKas()
        ).orElseGet(() -> {
            StatusBulan sb = new StatusBulan();
            sb.setUser(payer);
            sb.setKelas(t.getKelas());
            sb.setAngkatan(t.getAngkatan());
            sb.setBulan(req.getBulanKas());
            sb.setTahun(req.getTahunKas());
            return sb;
        });

        status.setIsPaid(true);
        status.setLastPayment(saved);
        status.setLastPaymentAt(Instant.now());
        statusBulanRepository.save(status);

        saveActivityLog(actor, "CREATE_TRANSAKSI", saved.getId());
        return saved;
    }

    private void saveActivityLog(User actor, String aksi, Long targetId) {
        ActivityLog log = ActivityLog.builder()
                .actor(actor)
                .aksi(aksi)
                .targetTable("transaksi")
                .targetId(targetId)
                .build();
        activityLogRepository.save(log);
    }
}