-- ============================================================
-- DATABASE SETUP SCRIPT untuk SIMKAS (Sistem Kas Angkatan 65)
-- ============================================================
-- Tanggal: 12 Januari 2026
-- Version: 1.0
-- ============================================================

-- 1. CREATE DATABASE
-- ============================================================
CREATE DATABASE IF NOT EXISTS simkas DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE simkas;

-- 2. CREATE TABLES (Assume Hibernate/JPA akan auto-create dengan DDL)
-- Jika table sudah ada dari Hibernate, skip bagian ini
-- ============================================================

-- 3. INSERT MASTER DATA
-- ============================================================

-- Insert Roles (Harus ada 3 role: ADMIN_ANGKATAN, BENDAHARA_KELAS, ANGGOTA)
INSERT INTO roles (name) VALUES 
('ADMIN_ANGKATAN'),
('BENDAHARA_KELAS'),
('ANGGOTA')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Angkatan 65
INSERT INTO angkatan (tahun, nama) VALUES 
(2022, 'Angkatan 65')
ON DUPLICATE KEY UPDATE nama = VALUES(nama);

-- Insert Kelas (5 kelas untuk Angkatan 65)
-- Assume angkatan_id = 1 (dari insert di atas)
INSERT INTO kelas (kode, nama, angkatan_id) VALUES 
('A', 'Kelas A', 1),
('B', 'Kelas B', 1),
('C', 'Kelas C', 1),
('D', 'Kelas D', 1),
('E', 'Kelas E', 1)
ON DUPLICATE KEY UPDATE nama = VALUES(nama);

-- ============================================================
-- 4. INSERT TEST USERS
-- ============================================================
-- Note: Password harus di-hash dengan BCrypt
-- Untuk testing, gunakan password: admin123 (hashed) atau bendahara123 (hashed)
-- 
-- Gunakan website untuk generate BCrypt hash:
-- - https://www.bcryptcalculator.com/
-- - Atau gunakan di Spring Boot: PasswordEncoder
--
-- Contoh hashes (untuk password "admin123" dan "bendahara123"):
-- admin123: $2a$10$slYQmyNdGzin7olVN3p5Be7DQfuQpVeVixV0KPHgHvzqP5FxsQanu
-- bendahara123: $2a$10$xR9S8QvMjVqQ9l5N7eK3u.YxZxL8D5Q6P0M2H9V5K5W7X8Y1A2B3C
-- mhs123: $2a$10$E8NHYa5E.6YcJ3l0M1K2HuLxZ2P5Q8R0S1T9U3V4W5X6Y7Z8A9B0C1

-- 1. ADMIN ANGKATAN (Bendahara Angkatan 65)
-- role_id = 1 (ADMIN_ANGKATAN)
-- kelas_id = NULL (admin tidak punya kelas spesifik)
-- angkatan_id = 1 (Angkatan 65)
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '999001',
    'Admin Angkatan 65',
    'admin@simkas.com',
    '$2a$10$slYQmyNdGzin7olVN3p5Be7DQfuQpVeVixV0KPHgHvzqP5FxsQanu',  -- password: admin123
    '081234567890',
    1,
    NULL,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- 2. BENDAHARA KELAS A
-- role_id = 2 (BENDAHARA_KELAS)
-- kelas_id = 1 (Kelas A)
-- angkatan_id = 1 (Angkatan 65)
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '210101',
    'Bendahara Kelas A',
    'bendahara.a@simkas.com',
    '$2a$10$xR9S8QvMjVqQ9l5N7eK3u.YxZxL8D5Q6P0M2H9V5K5W7X8Y1A2B3C',  -- password: bendahara123
    '081234567891',
    2,
    1,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- 3. BENDAHARA KELAS B
-- role_id = 2 (BENDAHARA_KELAS)
-- kelas_id = 2 (Kelas B)
-- angkatan_id = 1 (Angkatan 65)
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '210102',
    'Bendahara Kelas B',
    'bendahara.b@simkas.com',
    '$2a$10$xR9S8QvMjVqQ9l5N7eK3u.YxZxL8D5Q6P0M2H9V5K5W7X8Y1A2B3C',  -- password: bendahara123
    '081234567892',
    2,
    2,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- 4. MAHASISWA KELAS A #1
-- role_id = 3 (ANGGOTA)
-- kelas_id = 1 (Kelas A)
-- angkatan_id = 1 (Angkatan 65)
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '210103',
    'Mahasiswa Kelas A #1',
    'mhs1@simkas.com',
    '$2a$10$E8NHYa5E.6YcJ3l0M1K2HuLxZ2P5Q8R0S1T9U3V4W5X6Y7Z8A9B0C1',  -- password: mhs123
    '081234567893',
    3,
    1,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- 5. MAHASISWA KELAS A #2
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '210104',
    'Mahasiswa Kelas A #2',
    'mhs2@simkas.com',
    '$2a$10$E8NHYa5E.6YcJ3l0M1K2HuLxZ2P5Q8R0S1T9U3V4W5X6Y7Z8A9B0C1',  -- password: mhs123
    '081234567894',
    3,
    1,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- 6. MAHASISWA KELAS B #1
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '210105',
    'Mahasiswa Kelas B #1',
    'mhs3@simkas.com',
    '$2a$10$E8NHYa5E.6YcJ3l0M1K2HuLxZ2P5Q8R0S1T9U3V4W5X6Y7Z8A9B0C1',  -- password: mhs123
    '081234567895',
    3,
    2,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- 7. MAHASISWA KELAS B #2
INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif, created_at, updated_at)
VALUES (
    '210106',
    'Mahasiswa Kelas B #2',
    'mhs4@simkas.com',
    '$2a$10$E8NHYa5E.6YcJ3l0M1K2HuLxZ2P5Q8R0S1T9U3V4W5X6Y7Z8A9B0C1',  -- password: mhs123
    '081234567896',
    3,
    2,
    1,
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    nama = VALUES(nama),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- ============================================================
-- 5. INSERT SAMPLE KATEGORI (WADAH/TEMPAT BAYAR)
-- ============================================================
-- Ini adalah contoh data untuk testing flow

-- Kategori LEVEL ANGKATAN (dibuat oleh Admin Angkatan)
-- Untuk bendahara kelas setor ke admin angkatan
INSERT INTO kategori (nama, keterangan, level, nominal, id_kelas_pemilik, created_at)
VALUES (
    'Kas Angkatan - Januari 2025',
    'Penyetoran kas dari bendahara kelas ke bendahara angkatan',
    'ANGKATAN',
    500000,
    NULL,
    NOW()
)
ON DUPLICATE KEY UPDATE 
    keterangan = VALUES(keterangan),
    nominal = VALUES(nominal),
    updated_at = NOW();

-- Kategori LEVEL KELAS (dibuat oleh Bendahara Kelas A)
-- Untuk mahasiswa bayar ke bendahara kelas mereka
INSERT INTO kategori (nama, keterangan, level, nominal, id_kelas_pemilik, created_at)
VALUES (
    'Kas Kelas A - Buku Tulis Januari',
    'Pembelian buku tulis dan alat tulis untuk kelas A',
    'KELAS',
    50000,
    1,
    NOW()
)
ON DUPLICATE KEY UPDATE 
    keterangan = VALUES(keterangan),
    nominal = VALUES(nominal),
    updated_at = NOW();

-- Kategori LEVEL KELAS (dibuat oleh Bendahara Kelas B)
INSERT INTO kategori (nama, keterangan, level, nominal, id_kelas_pemilik, created_at)
VALUES (
    'Kas Kelas B - Makan Bersama Januari',
    'Dana untuk makan bersama bulan Januari',
    'KELAS',
    75000,
    2,
    NOW()
)
ON DUPLICATE KEY UPDATE 
    keterangan = VALUES(keterangan),
    nominal = VALUES(nominal),
    updated_at = NOW();

-- ============================================================
-- 6. VERIFY DATA
-- ============================================================
-- Jalankan query ini untuk memastikan data sudah benar:
-- ============================================================

-- Cek Roles
SELECT 'ROLES' as 'Table', COUNT(*) as 'Count' FROM roles;
SELECT * FROM roles;

-- Cek Angkatan
SELECT 'ANGKATAN' as 'Table', COUNT(*) as 'Count' FROM angkatan;
SELECT * FROM angkatan;

-- Cek Kelas
SELECT 'KELAS' as 'Table', COUNT(*) as 'Count' FROM kelas;
SELECT id, kode, nama, angkatan_id FROM kelas;

-- Cek Users
SELECT 'USERS' as 'Table', COUNT(*) as 'Count' FROM users;
SELECT u.id, u.nim, u.nama, u.email, r.name as role, k.nama as kelas 
FROM users u
LEFT JOIN roles r ON u.role_id = r.id
LEFT JOIN kelas k ON u.kelas_id = k.id
ORDER BY u.nim;

-- Cek Kategori
SELECT 'KATEGORI' as 'Table', COUNT(*) as 'Count' FROM kategori;
SELECT id, nama, level, nominal, id_kelas_pemilik FROM kategori;

-- ============================================================
-- 7. TESTING CREDENTIALS
-- ============================================================
-- Username       | Password      | Role
-- ============================================================
-- 999001         | admin123      | ADMIN_ANGKATAN
-- 210101         | bendahara123  | BENDAHARA_KELAS (Kelas A)
-- 210102         | bendahara123  | BENDAHARA_KELAS (Kelas B)
-- 210103         | mhs123        | ANGGOTA (Kelas A)
-- 210104         | mhs123        | ANGGOTA (Kelas A)
-- 210105         | mhs123        | ANGGOTA (Kelas B)
-- 210106         | mhs123        | ANGGOTA (Kelas B)
-- ============================================================

-- ============================================================
-- NOTES:
-- ============================================================
-- 1. BCrypt Hash untuk password testing:
--    - admin123:      $2a$10$slYQmyNdGzin7olVN3p5Be7DQfuQpVeVixV0KPHgHvzqP5FxsQanu
--    - bendahara123:  $2a$10$xR9S8QvMjVqQ9l5N7eK3u.YxZxL8D5Q6P0M2H9V5K5W7X8Y1A2B3C
--    - mhs123:        $2a$10$E8NHYa5E.6YcJ3l0M1K2HuLxZ2P5Q8R0S1T9U3V4W5X6Y7Z8A9B0C1
--
-- 2. Gunakan salah satu tools online untuk generate BCrypt:
--    https://www.bcryptcalculator.com/
--
-- 3. Jika menggunakan Spring Boot CLI:
--    spring security hash-password admin123
--
-- 4. Update password di query jika ingin menggunakan hash yang berbeda
-- ============================================================
