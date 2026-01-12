// File: src/main/java/com/polstat/simkas/service/UserService.java
package com.polstat.simkas.service;

import com.polstat.simkas.dto.UserDto;
import com.polstat.simkas.dto.UserProfileUpdateRequest;
import com.polstat.simkas.entity.Angkatan;
import com.polstat.simkas.entity.Kelas;
import com.polstat.simkas.entity.Role;
import com.polstat.simkas.entity.User;
import com.polstat.simkas.repository.AngkatanRepository;
import com.polstat.simkas.repository.KelasRepository;
import com.polstat.simkas.repository.RoleRepository;
import com.polstat.simkas.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KelasRepository kelasRepository;
    private final AngkatanRepository angkatanRepository;
    private final PasswordEncoder passwordEncoder;

    // KONSTANTA: Paksa ID 2 (Angkatan 65)
    private static final Long ID_ANGKATAN_65 = 2L;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       KelasRepository kelasRepository, AngkatanRepository angkatanRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.kelasRepository = kelasRepository;
        this.angkatanRepository = angkatanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user, Long roleId, Long kelasId, Long angkatanId) {
        // 1. Set Role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        user.setRole(role);

        // 2. Set Kelas (Jika dipilih)
        if (kelasId != null) {
            Kelas kelas = kelasRepository.findById(kelasId)
                    .orElseThrow(() -> new RuntimeException("Kelas not found with id: " + kelasId));
            user.setKelas(kelas);
        }

        // 3. [PERUBAHAN PENTING] Paksa Set Angkatan ke ID 2 (Angkatan 65)
        // Kita abaikan parameter 'angkatanId' dari inputan
        Angkatan angkatan = angkatanRepository.findById(ID_ANGKATAN_65)
                .orElseThrow(() -> new RuntimeException("Angkatan 65 (ID 2) tidak ditemukan di database!"));
        user.setAngkatan(angkatan);

        // 4. [WAJIB] Enkripsi Password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Mencari user berdasarkan NIM atau Email (Lebih fleksibel)
    public User getUserByUsername(String username) {
        return userRepository.findByNim(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public UserDto getUserProfileByUsername(String username) {
        return toDto(getUserByUsername(username));
    }

    @Transactional
    public UserDto updateUserProfile(String username, UserProfileUpdateRequest request) {
        User user = getUserByUsername(username);

        if (request.getNama() != null && !request.getNama().isEmpty()) user.setNama(request.getNama());
        if (request.getEmail() != null && !request.getEmail().isEmpty()) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        if (request.getKelasId() != null) {
            kelasRepository.findById(request.getKelasId()).ifPresent(user::setKelas);
        }

        // Angkatan tidak perlu diupdate karena sudah dikunci ke 65

        return toDto(userRepository.save(user));
    }

    @Transactional
    public void changeUserPassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new RuntimeException("Password lama salah");

        if (newPassword == null || newPassword.length() < 6)
            throw new RuntimeException("Password baru harus minimal 6 karakter");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUserAccount(String username) {
        User user = getUserByUsername(username);
        user.setAktif(false);
        userRepository.save(user);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setNim(user.getNim());
        dto.setNama(user.getNama());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRoleName(user.getRole() != null ? user.getRole().getName() : null);
        dto.setKelasId(user.getKelas() != null ? user.getKelas().getId() : null);
        dto.setAngkatanId(user.getAngkatan() != null ? user.getAngkatan().getId() : null);
        dto.setAktif(user.getAktif());
        return dto;
    }
}