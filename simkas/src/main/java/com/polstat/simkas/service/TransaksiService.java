package com.polstat.simkas.service;

import com.polstat.simkas.dto.HistoryTransaksi;
import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.dto.TransaksiResponse; // ✅ Fix: Gunakan TransaksiResponse
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // ✅ Fix: Import BigDecimal
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
    // LOGIC DASHBOARD / LIST DATA
    // ========================
    public List<Transaksi> findByUserIdAndActor(Long userId, User actor) {
        if (actor.getRole().getId() == 1) { // ADMIN
            if (actor.getAngkatan() == null) return new ArrayList<>();
            return transaksiRepository.findBendaharaTransactionsByAngkatan(actor.getAngkatan().getId());
        } else if (actor.getRole().getId() == 2) { // BENDAHARA
            if (actor.getKelas() == null) return new ArrayList<>();
            return transaksiRepository.findMahasiswaTransactionsByKelas(actor.getKelas().getId());
        } else { // MAHASISWA
            if (!actor.getId().equals(userId)) {
                throw new RuntimeException("Kamu tidak boleh lihat transaksi orang lain!");
            }
            return transaksiRepository.findByUserId(userId);
        }
    }

    // =================================================================
    // GET HISTORY (Dipanggil Controller untuk Android)
    // =================================================================
    public List<HistoryTransaksi> getHistoryByUserId(Long userId) {
        List<Transaksi> list = transaksiRepository.findByUserIdOrderByTanggalBayarDesc(userId);
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
    // CREATE TRANSAKSI
    // ========================
    @Transactional
    public Transaksi createTransaksiByRole(TransaksiRequest req, String nim) {
        User actor = getUserByUsername(nim);
        if (actor.getKelas() != null) {
            req.setIdKelas(actor.getKelas().getId());
        }
        return createTransaksiInternal(req, actor);
    }

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

        if (actor.getRole().getId() == 2 && !transaksi.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa mengupdate transaksi untuk kelasnya sendiri");
        }
        if (req.getNominal() != null) transaksi.setNominal(req.getNominal());
        if (req.getKeterangan() != null) transaksi.setKeterangan(req.getKeterangan());
        if (req.getIdKategori() != null)
            kategoriRepository.findById(req.getIdKategori()).ifPresent(transaksi::setKategori);

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
        transaksiRepository.delete(transaksi);
        saveActivityLog(actor, "DELETE_TRANSAKSI", id);
    }

    // ========================
    // LOGIC VALIDASI (TERIMA/TOLAK)
    // ========================
    @Transactional
    public Transaksi validateTransaksi(Long id, String status, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        if (actor.getRole().getId() == 2 && !transaksi.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa memvalidasi transaksi kelasnya");
        }

        transaksi.setStatusValidasi(Transaksi.StatusValidasi.valueOf(status.toUpperCase()));
        Transaksi updated = transaksiRepository.save(transaksi);
        saveActivityLog(actor, "VALIDATE_TRANSAKSI", updated.getId());
        return updated;
    }

    // Method Baru: Return DTO untuk Endpoint Validasi
    public TransaksiResponse validasiTransaksi(Long idTransaksi, String statusBaru) {
        Transaksi transaksi = transaksiRepository.findById(idTransaksi)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        if (!"VALID".equalsIgnoreCase(statusBaru) && !"REJECTED".equalsIgnoreCase(statusBaru)) {
            throw new RuntimeException("Status tidak valid! Hanya boleh VALID atau REJECTED.");
        }

        // ✅ Fix: Convert String ke Enum
        transaksi.setStatusValidasi(Transaksi.StatusValidasi.valueOf(statusBaru.toUpperCase()));
        transaksiRepository.save(transaksi);

        return toDto(transaksi); // ✅ Fix: Panggil method toDto di bawah
    }

    // Method Baru: Get Pending List
    @Transactional(readOnly = true)
    public List<TransaksiResponse> getPendingTransaksiByKategori(Long idKategori) {
        return transaksiRepository.findAll().stream()
                .filter(t -> t.getKategori() != null && t.getKategori().getId().equals(idKategori))
                .filter(t -> t.getStatusValidasi() != null && "PENDING".equals(t.getStatusValidasi().name()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Method Baru: Hitung Total Valid
    public BigDecimal hitungTotalPemasukanKategori(Long idKategori) {
        return transaksiRepository.findAll().stream()
                .filter(t -> t.getKategori() != null && t.getKategori().getId().equals(idKategori))
                .filter(t -> t.getStatusValidasi() != null && "VALID".equals(t.getStatusValidasi().name()))
                .map(Transaksi::getNominal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========================
    // HELPER METHODS
    // ========================
    @Transactional
    private Transaksi createTransaksiInternal(TransaksiRequest req, User actor) {
        Transaksi t = new Transaksi();
        User payer = userRepository.findById(req.getIdUser())
                .orElseThrow(() -> new RuntimeException("User pembayar tidak ditemukan"));

        t.setUser(payer);
        t.setInputBy(actor);

        if (req.getIdKelas() != null) kelasRepository.findById(req.getIdKelas()).ifPresent(t::setKelas);
        else if (payer.getKelas() != null) t.setKelas(payer.getKelas());

        if (req.getIdAngkatan() != null) angkatanRepository.findById(req.getIdAngkatan()).ifPresent(t::setAngkatan);
        else if (payer.getKelas() != null && payer.getKelas().getAngkatan() != null) t.setAngkatan(payer.getKelas().getAngkatan());

        if (req.getIdKategori() != null) kategoriRepository.findById(req.getIdKategori()).ifPresent(t::setKategori);

        t.setBulanKas(req.getBulanKas());
        t.setTahunKas(req.getTahunKas());
        t.setNominal(req.getNominal());
        t.setKeterangan(req.getKeterangan());
        t.setMetodePembayaran("TRANSFER");
        t.setTanggalBayar(Instant.now());
        t.setStatusValidasi(Transaksi.StatusValidasi.PENDING);
        t.setJenisTransaksi(req.getJenisTransaksi());
        t.setBuktiBayar(req.getBuktiBayar());

        Transaksi saved = transaksiRepository.save(t);
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

    // ✅ Fix: Method Helper toDto yang sebelumnya hilang
    private TransaksiResponse toDto(Transaksi t) {
        TransaksiResponse r = new TransaksiResponse();
        r.setId(t.getId());
        r.setIdUser(t.getUser() != null ? t.getUser().getId() : null);
        r.setNominal(t.getNominal());
        r.setTanggalBayar(t.getTanggalBayar());
        r.setKeterangan(t.getKeterangan());
        r.setJenisTransaksi(t.getJenisTransaksi());
        r.setStatusValidasi(t.getStatusValidasi() != null ? t.getStatusValidasi().name() : null);
        r.setBuktiBayar(t.getBuktiBayar());
        return r;
    }
}