package com.polstat.simkas.service;

import com.polstat.simkas.dto.HistoryTransaksi;
import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.dto.TransaksiResponse;
import com.polstat.simkas.entity.*;
import com.polstat.simkas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    // ========================
    // GET HISTORY (Android)
    // ========================
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

    // ========================
    // UPDATE, DELETE, VALIDATE
    // ========================
    @Transactional
    public Transaksi updateTransaksiByRole(Long id, TransaksiRequest req, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

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
        transaksiRepository.delete(transaksi);
        saveActivityLog(actor, "DELETE_TRANSAKSI", id);
    }

    @Transactional
    public Transaksi validateTransaksi(Long id, String status, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        transaksi.setStatusValidasi(Transaksi.StatusValidasi.valueOf(status.toUpperCase()));
        Transaksi updated = transaksiRepository.save(transaksi);
        saveActivityLog(actor, "VALIDATE_TRANSAKSI", updated.getId());
        return updated;
    }

    // Endpoint Validasi (Return DTO)
    public TransaksiResponse validasiTransaksi(Long idTransaksi, String statusBaru) {
        Transaksi transaksi = transaksiRepository.findById(idTransaksi)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        if (!"VALID".equalsIgnoreCase(statusBaru) && !"REJECTED".equalsIgnoreCase(statusBaru)) {
            throw new RuntimeException("Status tidak valid! Hanya boleh VALID atau REJECTED.");
        }
        transaksi.setStatusValidasi(Transaksi.StatusValidasi.valueOf(statusBaru.toUpperCase()));
        transaksiRepository.save(transaksi);
        return toDto(transaksi);
    }

    // ========================
    // LOGIC PER KATEGORI (WADAH)
    // ========================
    @Transactional(readOnly = true)
    public List<TransaksiResponse> getTransaksiByKategori(Long idKategori) {
        List<Transaksi> list = transaksiRepository.findByKategoriId(idKategori);
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransaksiResponse> getPendingTransaksiByKategori(Long idKategori) {
        return transaksiRepository.findAll().stream()
                .filter(t -> t.getKategori() != null && t.getKategori().getId().equals(idKategori))
                .filter(t -> t.getStatusValidasi() != null && "PENDING".equals(t.getStatusValidasi().name()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BigDecimal hitungTotalPemasukanKategori(Long idKategori) {
        return transaksiRepository.sumTotalValidByKategori(idKategori);
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

        // Data tambahan untuk Dashboard/Detail
        if (t.getUser() != null) {
            // ✅ PERBAIKAN: getNamaLengkap() diganti menjadi getNama()
            r.setNamaPengirim(t.getUser().getNama());
            r.setNimPengirim(t.getUser().getNim());
        }
        if (t.getKelas() != null) r.setNamaKelas(t.getKelas().getNama());
        if (t.getKategori() != null) r.setNamaWadah(t.getKategori().getNama());

        return r;
    }
}