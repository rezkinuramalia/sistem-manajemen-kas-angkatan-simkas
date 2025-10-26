package com.polstat.simkas.service;

import com.polstat.simkas.dto.TransaksiRequest;
import com.polstat.simkas.entity.ActivityLog;
import com.polstat.simkas.entity.StatusBulan;
import com.polstat.simkas.entity.Transaksi;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    // CREATE TRANSAKSI (Bendahara Kelas)
    // ========================
    @Transactional
    public Transaksi createTransaksiByRole(TransaksiRequest req, String nim) {
        User actor = getUserByUsername(nim);

        if (actor.getRole().getId() != 2) { // bukan bendahara
            throw new RuntimeException("Hanya bendahara kelas yang bisa pakai endpoint ini");
        }

        // otomatis detect kelas bendahara
        Long kelasBendaharaId = actor.getKelas().getId();
        if (!req.getIdKelas().equals(kelasBendaharaId)) {
            throw new RuntimeException("Bendahara hanya bisa menambah transaksi untuk kelasnya sendiri");
        }

        return createTransaksiInternal(req, actor);
    }

    // ========================
// CREATE TRANSAKSI (Admin Angkatan /kelas/admin)
// ========================
    @Transactional
    public Transaksi createTransaksiAdminAngkatan(TransaksiRequest req, String nim) {
        User admin = getUserByUsername(nim);

        if (!"ADMIN_ANGKATAN".equals(admin.getRole().getName())) { // cek berdasarkan role name
            throw new RuntimeException("Hanya admin angkatan yang bisa pakai endpoint ini");
        }

        User payer = userRepository.findById(req.getIdUser())
                .orElseThrow(() -> new RuntimeException("User pembayar tidak ditemukan"));

        if (payer.getKelas() == null) {
            throw new RuntimeException("User pembayar belum memiliki kelas");
        }

        // cek apakah user pembayar berada di angkatan admin
        if (!payer.getKelas().getAngkatan().getId().equals(admin.getAngkatan().getId())) {
            throw new RuntimeException("User pembayar bukan berasal dari angkatan admin");
        }

        // otomatis set idKelas dari kelas user
        req.setIdKelas(payer.getKelas().getId());

        return createTransaksiInternal(req, admin);
    }


    // ========================
    // UPDATE, DELETE, VALIDATE (sama seperti sebelumnya)
    // ========================
    @Transactional
    public Transaksi updateTransaksiByRole(Long id, TransaksiRequest req, String nim) {
        User actor = getUserByUsername(nim);
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

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

        if (actor.getRole().getId() == 2 && !transaksi.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa memvalidasi transaksi untuk kelasnya sendiri");
        }
        if (actor.getRole().getId() == 1 && !transaksi.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
            throw new RuntimeException("Admin angkatan hanya bisa memvalidasi transaksi untuk kelas di angkatannya sendiri");
        }

        transaksi.setStatusValidasi(Transaksi.StatusValidasi.valueOf(status.toUpperCase()));
        transaksi.setInputBy(actor);
        Transaksi updated = transaksiRepository.save(transaksi);
        saveActivityLog(actor, "VALIDATE_TRANSAKSI", updated.getId());
        return updated;
    }

    // ========================
    // LAPORAN, FIND, HELPERS
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

        if (actor.getRole().getId() == 2) {
            if (!targetUser.getKelas().getId().equals(actor.getKelas().getId())) {
                throw new RuntimeException("Bendahara hanya bisa melihat transaksi user di kelasnya sendiri");
            }
        } else if (actor.getRole().getId() == 1) {
            if (!targetUser.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
                throw new RuntimeException("Admin angkatan hanya bisa melihat transaksi user di angkatannya sendiri");
            }
        }

        List<Transaksi> list = findByUserId(userId);
        return filterByBulanJenisTahun(list, bulan, tahun, jenisTransaksi);
    }

    public List<Transaksi> findMeByUserId(Long userId, String username) {
        User actor = getUserByUsername(username);
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (actor.getRole().getId() == 2 && !targetUser.getKelas().getId().equals(actor.getKelas().getId())) {
            throw new RuntimeException("Bendahara hanya bisa melihat transaksi user di kelasnya sendiri");
        }
        if (actor.getRole().getId() == 1 && !targetUser.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
            throw new RuntimeException("Admin angkatan hanya bisa melihat transaksi user di angkatannya sendiri");
        }
        if (actor.getRole().getId() != 1 && actor.getRole().getId() != 2) {
            if (!actor.getId().equals(userId)) {
                throw new RuntimeException("Kamu tidak boleh lihat transaksi orang lain!");
            }
        }

        return findByUserId(userId);
    }

    @Transactional
    private Transaksi createTransaksiInternal(TransaksiRequest req, User actor) {
        Transaksi t = new Transaksi();
        User payer = userRepository.findById(req.getIdUser())
                .orElseThrow(() -> new RuntimeException("User pembayar tidak ditemukan"));
        t.setUser(payer);
        t.setInputBy(actor);

        if (req.getIdKelas() != null) {
            kelasRepository.findById(req.getIdKelas()).ifPresent(t::setKelas);
        } else if (payer.getKelas() != null) {
            t.setKelas(payer.getKelas());
        } else {
            throw new RuntimeException("Kelas harus ditentukan, user pembayar belum punya kelas");
        }

        angkatanRepository.findById(req.getIdAngkatan()).ifPresent(t::setAngkatan);
        kategoriRepository.findById(req.getIdKategori()).ifPresent(t::setKategori);

        t.setBulanKas(req.getBulanKas());
        t.setTahunKas(req.getTahunKas());
        t.setNominal(req.getNominal());
        t.setKeterangan(req.getKeterangan());
        t.setMetodePembayaran(req.getMetodePembayaran());
        t.setTanggalBayar(Instant.now());
        t.setStatusValidasi(Transaksi.StatusValidasi.PENDING);
        t.setJenisTransaksi(req.getJenisTransaksi());

        Transaksi saved = transaksiRepository.save(t);

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

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    public List<Transaksi> findByUserIdAndActor(Long userId, User actor) {
        User targetUser = getUserById(userId);

        if (actor.getRole().getId() == 2) { // Bendahara
            if (!targetUser.getKelas().getId().equals(actor.getKelas().getId())) {
                throw new RuntimeException("Bendahara hanya bisa lihat transaksi user di kelasnya sendiri");
            }
        } else if (actor.getRole().getId() == 1) { // Admin Angkatan
            if (!targetUser.getAngkatan().getId().equals(actor.getAngkatan().getId())) {
                throw new RuntimeException("Admin angkatan hanya bisa lihat transaksi user di angkatannya sendiri");
            }
        }

        return transaksiRepository.findByUserId(userId);
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findByNim(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    public List<Transaksi> laporanAngkatan(String username, Integer bulan, Integer tahun, String jenisTransaksi) {
        User admin = getUserByUsername(username);
        if (admin.getAngkatan() == null) {
            throw new RuntimeException("Admin angkatan tidak memiliki angkatan");
        }
        List<Transaksi> list = findByAngkatan(admin.getAngkatan().getId(), null);
        return filterByBulanJenisTahun(list, bulan, tahun, jenisTransaksi);
    }
}
