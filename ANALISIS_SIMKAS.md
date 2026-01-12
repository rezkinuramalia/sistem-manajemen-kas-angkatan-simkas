# 📋 ANALISIS LENGKAP PROJECT SIMKAS (Sistem Informasi Manajemen Kas)

**Tanggal Analisis:** 12 Januari 2026  
**Status:** Analisis Komprehensif Selesai  
**Tipe Project:** Android Mobile App + Spring Boot Backend

---

## 🎯 DAFTAR ISI
1. [Ringkasan Kebutuhan](#ringkasan-kebutuhan)
2. [Arsitektur Project](#arsitektur-project)
3. [Analisis Issue & Bug](#analisis-issue--bug)
4. [Perbaikan yang Diperlukan](#perbaikan-yang-diperlukan)
5. [Checklist Implementasi](#checklist-implementasi)
6. [Instruksi Setup & Testing](#instruksi-setup--testing)

---

## 📝 RINGKASAN KEBUTUHAN

### Spesifikasi Aplikasi
- **Nama:** SIMKAS (Sistem Informasi Manajemen Kas)
- **Scope:** Khusus Angkatan 65
- **Platform:** Mobile (Android)
- **Backend:** Spring Boot (IntelliJ IDEA Community)
- **Database:** MySQL via Laragon
- **Network:** IP Laptop: 192.168.1.5

### 3 Role & Tanggung Jawab

| Role | Menu | Fungsi Utama |
|------|------|-------------|
| **ADMIN ANGKATAN** (Bendahara Angkatan) | 1. Profil<br>2. Tambah Tempat Bayar<br>3. Beranda (Dashboard)<br>4. History Transaksi | Mengelola kas angkatan keseluruhan, menerima setoran dari bendahara kelas, membuat wadah/form penyetoran |
| **BENDAHARA_KELAS** (Bendahara Kelas) | 1. Profil<br>2. Tambah Tempat Bayar (+)<br>3. Tempat Penyetoran<br>4. Beranda (Dashboard)<br>5. History Transaksi | Mengelola kas kelas, membuat wadah penyetoran untuk mahasiswa, menyetor ke bendahara angkatan |
| **ANGGOTA** (Mahasiswa) | 1. Profil<br>2. Setor (Form Pembayaran)<br>3. Riwayat | Melihat wadah penyetoran kelasnya, menyetor kas, melihat history pembayaran |

### Authentication
- **Login:** NIM atau Email + Password
- **Register:** Hanya untuk role ANGGOTA (Mahasiswa)
- **Akun Admin/Bendahara:** Dibuat manual di database

---

## 🏗️ ARSITEKTUR PROJECT

### Backend Structure (Spring Boot)
```
simkas/
├── src/main/java/com/polstat/simkas/
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── KategoriController.java (DEPRECATED - gunakan MasterDataController)
│   │   ├── MasterDataController.java (UTAMA untuk Kategori, Kelas, Angkatan)
│   │   ├── DashboardController.java
│   │   └── TransaksiController.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Kelas.java
│   │   ├── Angkatan.java
│   │   ├── Kategori.java (Wadah/Tempat Bayar)
│   │   ├── Transaksi.java (Laporan Pembayaran)
│   │   ├── StatusBulan.java
│   │   └── ActivityLog.java
│   ├── dto/
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── LoginRequest.java (ALIAS dari AuthRequest)
│   │   ├── RegisterRequest.java
│   │   ├── UserDto.java
│   │   ├── UserProfileUpdateRequest.java
│   │   ├── KategoriRequest.java
│   │   ├── KategoriDto.java
│   │   ├── TransaksiRequest.java
│   │   ├── TransaksiResponse.java
│   │   ├── DashboardKelasResponse.java
│   │   ├── DashboardAngkatanResponse.java
│   │   ├── HistoryTransaksi.java
│   │   └── ... (lainnya)
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── KategoriRepository.java
│   │   ├── TransaksiRepository.java
│   │   ├── KelasRepository.java
│   │   ├── AngkatanRepository.java
│   │   ├── RoleRepository.java
│   │   └── ... (lainnya)
│   ├── service/
│   │   ├── UserService.java
│   │   ├── TransaksiService.java
│   │   ├── MasterDataService.java
│   │   ├── DashboardService.java
│   │   ├── MyUserDetailsService.java
│   │   └── ... (lainnya)
│   ├── util/
│   │   └── JwtUtil.java
│   ├── config/
│   │   └── SecurityConfig.java
│   └── SimKasApplication.java
└── src/main/resources/
    └── application.properties
```

### Android Structure (Jetpack Compose)
```
SimkasApp/app/src/main/java/com/example/simkasapp/
├── api/
│   ├── RetrofitClient.kt
│   └── ApiService.kt
├── models/
│   └── Models.kt (Semua data class)
├── screens/
│   ├── WelcomeScreen.kt
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   ├── MainContainerScreen.kt (Scaffold dengan Navigation)
│   ├── DashboardScreen.kt
│   ├── ProfileScreen.kt
│   ├── CreateKategoriScreen.kt (Buat Wadah Penyetoran)
│   ├── WadahListScreen.kt (List Wadah/Tempat Bayar)
│   ├── WadahDetailScreen.kt (Detail Wadah & Form Bayar)
│   ├── UploadScreen.kt (Upload Bukti Bayar)
│   └── ... (screen lainnya)
├── ui/
│   └── theme/
│       └── Theme.kt (BpsBlue, BpsOrange, BpsGreen)
├── utils/
│   └── (utils functions)
└── MainActivity.kt
```

---

## 🐛 ANALISIS ISSUE & BUG

### Critical Issues

#### 1. ⚠️ **IP ADDRESS SALAH DI RetrofitClient.kt**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/api/RetrofitClient.kt`

```kotlin
// ❌ WRONG - IP lama
private const val BASE_URL = "http://10.100.162.8:8080/"

// ✅ CORRECT - Sesuai IP laptop user
private const val BASE_URL = "http://192.168.1.5:8080/"
```

**Impact:** Android app tidak bisa terhubung ke backend → Login/API calls gagal  
**Priority:** 🔴 CRITICAL

---

#### 2. ⚠️ **Database Configuration di application.properties**
**File:** `simkas/src/main/resources/application.properties`

```properties
# Pastikan konfigurasi MySQL sudah BENAR untuk Laragon
spring.datasource.url=jdbc:mysql://localhost:3306/simkas?useSSL=false&serverTimezone=Asia/Jakarta
spring.datasource.username=root
spring.datasource.password=     # KOSONG jika Laragon default
```

**Checklist:**
- [ ] Pastikan Laragon MySQL running di port 3306
- [ ] Database `simkas` sudah dibuat
- [ ] Username/password sesuai dengan config Laragon

---

#### 3. ⚠️ **Role Naming Inconsistency**
**Backend Roles:**
- `ADMIN_ANGKATAN` (tidak ada di source tapi mesti ada di database)
- `BENDAHARA_KELAS`
- `ANGGOTA`

**Periksa di Database:**
```sql
SELECT * FROM roles;
-- Harus ada 3 record dengan id dan name:
-- id=1, name='ADMIN_ANGKATAN'
-- id=2, name='BENDAHARA_KELAS'
-- id=3, name='ANGGOTA'
```

**Android Code** harus konsisten dengan role name ini.

**Priority:** 🟠 HIGH

---

#### 4. ⚠️ **Token Bearer Format Inconsistency**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/screens/ProfileScreen.kt`

**Problem:**
```kotlin
// Backend mengirim token raw (tanpa "Bearer ")
// Android perlu menambahkan "Bearer " prefix untuk request selanjutnya

val finalToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
```

**Perlu di-standardkan:**
- Pilihan 1: Backend langsung return token dengan "Bearer " prefix
- Pilihan 2: Android handling di RetrofitClient dengan Interceptor

**Recommendation:** Gunakan Interceptor di RetrofitClient agar auto-add "Bearer "

---

#### 5. ⚠️ **Kategori (Wadah) - Field Nominal Hilang**
**Problem:** Spesifikasi user menyebutkan:
> "Tempat penyetoran berisi judul pembayaran, deskripsi pembayaran, dan **nominal pembayaran**"

Tapi `Kategori.java` entity tidak punya field `nominal`:
```java
@Entity
public class Kategori {
    private Long id;
    private String nama;
    private String keterangan;
    private String level;
    private Long idKelasPemilik;
    // ❌ MISSING: nominal pembayaran
}
```

**Fix Required:** Tambahkan field nominal ke Kategori entity:
```java
@Column(nullable = false, precision = 12, scale = 2)
private BigDecimal nominal;
```

---

#### 6. ⚠️ **Android Models.kt - Field Ketidaksesuaian**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/models/Models.kt`

```kotlin
// ❌ WRONG - Kategori tidak punya nominal di model Android
data class Kategori(
    val id: Int,
    val nama: String,
    val keterangan: String?,
    val level: String?,
    val totalTerkumpul: Double
) : Serializable

// ✅ SHOULD BE - Tambahkan nominal
data class Kategori(
    val id: Int,
    val nama: String,
    val keterangan: String?,
    val level: String?,
    val nominal: Double,  // ← TAMBAHKAN INI
    val totalTerkumpul: Double
) : Serializable
```

**Impact:** Form pembayaran tidak tahu nominal yang harus dibayar

---

#### 7. ⚠️ **TransaksiRequest vs TransaksiResponse Mismatch**
**Android DTO:**
```kotlin
// TransaksiRequest
data class TransaksiRequest(
    val idUser: Int,
    val nominal: Double,
    val bulanKas: Int,
    val tahunKas: Int,
    val keterangan: String,
    val jenisTransaksi: String,
    val idKategori: Int,
    val idKelas: Int?,
    val idAngkatan: Int?
)

// ❌ WRONG - Attribute tidak konsisten dengan backend
```

**Backend TransaksiRequest:**
```java
// Harus match dengan Android DTO
```

**Need to verify:** Apakah semua field di Android match dengan yang diterima backend controller?

---

#### 8. ⚠️ **UploadScreen.kt - Hardcoded Values**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/screens/UploadScreen.kt`

```kotlin
// ❌ WRONG - Hardcoded bulan dan tahun
"bulanKas": 1, 
"tahunKas": 2025

// ✅ SHOULD BE - Dinamis dari Kalender
```

Seharusnya ambil dari sistem tanggal saat ini:
```kotlin
import java.time.YearMonth
val now = YearMonth.now()
"bulanKas": ${now.monthValue}, 
"tahunKas": ${now.year}
```

---

#### 9. ⚠️ **SharedPreferences - UserId Tidak Disimpan**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/screens/LoginScreen.kt`

```kotlin
// ❌ WRONG - Hanya simpan TOKEN & ROLE, tidak ID user
prefs.edit()
    .putString("TOKEN", token)
    .putString("ROLE", role)
    .apply()

// ✅ SHOULD BE - Tambahkan userId
val userId = response.body()?.id  // Pastikan LoginResponse punya field id
prefs.edit()
    .putString("TOKEN", token)
    .putString("ROLE", role)
    .putInt("ID_USER", userId)
    .apply()
```

**Need to verify:** Apakah `LoginResponse` punya field `id`?

---

#### 10. ⚠️ **ApiService.kt - Endpoint Paths**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/api/ApiService.kt`

```kotlin
// Verify semua endpoint ini ada di backend:

@POST("api/auth/login")
fun login(@Body request: LoginRequest): Call<LoginResponse>  ✓

@POST("api/auth/register")
fun register(@Body request: RegisterRequest): Call<String>  ✓

@GET("api/users/profile")
fun getMyProfile(@Header("Authorization") token: String): Call<UserDto>  ✓

@PUT("api/users/profile")
fun updateProfile(@Header("Authorization") token: String, @Body req: UserProfileUpdateRequest): Call<UserDto>  ✓

@GET("api/master/kategori")
fun getAllKategori(@Header("Authorization") token: String): Call<List<Kategori>>  
// ⚠️ VERIFY: Backend punya endpoint ini?

@POST("api/master/kategori")
fun createKategori(@Header("Authorization") token: String, @Body req: KategoriRequest): Call<Kategori>
// ⚠️ VERIFY: Backend punya endpoint ini?

@GET("api/master/kelas")
fun getAllKelas(@Header("Authorization") token: String): Call<List<Kelas>>  ✓

@GET("api/master/angkatan")
fun getAllAngkatan(@Header("Authorization") token: String): Call<List<Angkatan>>  ✓

@GET("api/dashboard/kelas")
fun getDashboardKelas(@Header("Authorization") token: String): Call<DashboardKelasResponse>  ✓

@GET("api/dashboard/angkatan")
fun getDashboardAngkatan(@Header("Authorization") token: String): Call<DashboardAngkatanResponse>  ✓

@Multipart
@POST("api/transaksi")
fun createTransaksi(
    @Header("Authorization") token: String,
    @Part("data") data: RequestBody,
    @Part file: MultipartBody.Part
): Call<TransaksiResponse>
// ⚠️ VERIFY: Path di controller cocok? (Ada prefix /api?)

@GET("api/transaksi/history")
fun getMyHistory(@Header("Authorization") token: String): Call<List<HistoryTransaksi>>  ✓
```

---

#### 11. ⚠️ **MainContainerScreen Navigation Issues**
**File:** `SimkasApp/app/src/main/java/com/example/simkasapp/screens/MainContainerScreen.kt`

**Problem:** Pastikan route navigation cocok dengan screen composable yang tersedia.

**Common Issues:**
- Route names typo
- Missing screen implementations
- Wrong parameter passing

---

## 🔧 PERBAIKAN YANG DIPERLUKAN

### Phase 1: CRITICAL (Wajib diperbaiki hari ini)

#### P1.1: Update RetrofitClient IP Address
```kotlin
// File: SimkasApp/app/src/main/java/com/example/simkasapp/api/RetrofitClient.kt
private const val BASE_URL = "http://192.168.1.5:8080/"
```

#### P1.2: Add Bearer Token Interceptor
Tambahkan di RetrofitClient untuk auto-add "Bearer " prefix:
```kotlin
val httpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val original = chain.request()
        val token = prefs.getString("TOKEN", "") ?: ""
        val request = original.newBuilder()
            .header("Authorization", if (!token.startsWith("Bearer ")) "Bearer $token" else token)
            .build()
        chain.proceed(request)
    }
    .build()

val instance: ApiService by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
```

#### P1.3: Add Nominal Field ke Kategori Entity (Backend)
```java
// File: simkas/src/main/java/com/polstat/simkas/entity/Kategori.java
@Column(nullable = false, precision = 12, scale = 2)
private BigDecimal nominal;
```

#### P1.4: Update Kategori Model di Android
```kotlin
// File: SimkasApp/app/src/main/java/com/example/simkasapp/models/Models.kt
data class Kategori(
    val id: Int,
    val nama: String,
    val keterangan: String?,
    val level: String?,
    val nominal: Double,
    val totalTerkumpul: Double
) : Serializable
```

---

### Phase 2: HIGH (Sebelum testing dimulai)

#### P2.1: Standardize LoginResponse
Backend harus return user id juga:
```java
// File: simkas/src/main/java/com/polstat/simkas/dto/AuthResponse.java
public class AuthResponse {
    private String token;
    private String username;
    private Long id;
    private String role;
    // getter/setter
}
```

#### P2.2: Update Android LoginResponse Model
```kotlin
data class LoginResponse(
    val token: String,
    val username: String,
    val id: Int,
    val role: String?
)
```

#### P2.3: Update LoginScreen untuk menyimpan userId
```kotlin
prefs.edit()
    .putString("TOKEN", token)
    .putString("ROLE", role)
    .putInt("ID_USER", body?.id ?: 0)
    .apply()
```

#### P2.4: Fix UploadScreen - Dynamic Date
```kotlin
import java.time.YearMonth
import java.time.LocalDate

val now = LocalDate.now()
val jsonString = """
{
    "idUser": $userId, 
    "idKategori": ${selectedKategori!!.id},
    "nominal": ${nominal},
    "keterangan": "$keterangan",
    "jenisTransaksi": "PEMASUKAN",
    "bulanKas": ${now.monthValue}, 
    "tahunKas": ${now.year}
}
""".trimIndent()
```

#### P2.5: Add Serializable ke LoginResponse & semua API Response
```kotlin
data class LoginResponse(
    val token: String,
    val username: String,
    val id: Int,
    val role: String?
) : Serializable
```

---

### Phase 3: MEDIUM (Sebelum production)

#### P3.1: Implement Proper Multipart File Upload
Ensure TransaksiController.java properly handles multipart:
```java
// Verify ini sudah implemented di controller
@PostMapping(value = "/transaksi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> createTransaksi(
        @RequestPart("data") String transaksiJson,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Authentication authentication)
```

#### P3.2: Add Error Handling & Logging
Tambahkan logging di semua network calls untuk debugging:
```kotlin
override fun onFailure(call: Call<T>, t: Throwable) {
    Log.e("API_ERROR", "Endpoint: ${call.request().url}, Error: ${t.message}")
    isLoading = false
}
```

#### P3.3: Add Session Management
Implement logout & clear SharedPreferences:
```kotlin
fun doLogout() {
    prefs.edit().clear().apply()
    navController.navigate("welcome") {
        popUpTo("dashboard") { inclusive = true }
    }
}
```

#### P3.4: Role-Based UI Visibility
Pastikan setiap screen hanya tampil untuk role yang tepat (sudah implemented di DashboardScreen)

#### P3.5: Add Input Validation
- Nominal: must be > 0
- NIM: length validation
- Email: format validation
- Password: strength validation

---

## ✅ CHECKLIST IMPLEMENTASI

### Backend Setup
- [ ] MySQL Laragon running di port 3306
- [ ] Database `simkas` created
- [ ] Run migration/DDL untuk semua table
- [ ] Insert seed data:
  - [ ] Roles (ADMIN_ANGKATAN, BENDAHARA_KELAS, ANGGOTA)
  - [ ] Angkatan 65
  - [ ] Sample Kelas (A, B, C, D, E)
  - [ ] Sample Users (1 admin angkatan, 2-3 bendahara kelas, 5+ mahasiswa)
- [ ] Compile & Run Spring Boot di port 8080
- [ ] Test API endpoints via Postman/Insomnia
- [ ] Verify JWT token generation working
- [ ] Verify file upload path (`uploads/bukti-bayar/`) writable

### Android Setup
- [ ] Update IP di RetrofitClient.kt (192.168.1.5)
- [ ] Add Bearer Interceptor di RetrofitClient.kt
- [ ] Update all Models.kt dengan complete fields
- [ ] Update LoginResponse model dengan id field
- [ ] Update Kategori model dengan nominal field
- [ ] Fix UploadScreen date handling
- [ ] Compile & Run Android Studio
- [ ] Test Login with valid credentials
- [ ] Test Register (should create ANGGOTA role user)
- [ ] Test Profile loading & editing
- [ ] Test Kategori/Wadah creation (as ADMIN_ANGKATAN/BENDAHARA_KELAS)
- [ ] Test Kategori list (filtered by role)
- [ ] Test Transaksi/Upload bukti bayar
- [ ] Test Dashboard rendering per role
- [ ] Test History transaksi display

### Integration Testing
- [ ] Test Login → Dashboard flow
- [ ] Test Admin Angkatan: Create Wadah → View di list
- [ ] Test Bendahara Kelas: Create Wadah → Mahasiswa bisa lihat
- [ ] Test Mahasiswa: View Wadah → Upload bukti → History
- [ ] Test Cross-role filtering (Mhs tidak lihat wadah angkatan, dll)
- [ ] Test Token refresh/expiration
- [ ] Test File upload persistence (check `uploads/bukti-bayar/` folder)
- [ ] Test Database consistency after transactions

---

## 📝 INSTRUKSI SETUP & TESTING

### Backend Setup (IntelliJ IDEA Community)

1. **Open Project**
   ```bash
   cd d:\Amel\Kuliah\Semester 5\Pemrograman Platform Khusus\simkas\simkas
   ```

2. **Configure Database**
   - Buka Laragon, pastikan MySQL running
   - Create database:
     ```sql
     CREATE DATABASE simkas DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
     ```

3. **Create Tables** (Database seed)
   ```sql
   USE simkas;

   -- Roles
   INSERT INTO roles (name) VALUES 
   ('ADMIN_ANGKATAN'),
   ('BENDAHARA_KELAS'),
   ('ANGGOTA');

   -- Angkatan
   INSERT INTO angkatan (tahun, nama) VALUES 
   (2022, 'Angkatan 65');

   -- Kelas
   INSERT INTO kelas (kode, nama, angkatan_id) VALUES 
   ('A', 'Kelas A', 1),
   ('B', 'Kelas B', 1),
   ('C', 'Kelas C', 1);

   -- Users
   -- ADMIN ANGKATAN
   INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif)
   VALUES ('999001', 'Admin Angkatan', 'admin@simkas.com', '$2a$10$...', '081234567890', 1, NULL, 1, true);

   -- BENDAHARA KELAS A
   INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif)
   VALUES ('210101', 'Bendahara Kelas A', 'bendahara.a@simkas.com', '$2a$10$...', '081234567891', 2, 1, 1, true);

   -- BENDAHARA KELAS B
   INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif)
   VALUES ('210102', 'Bendahara Kelas B', 'bendahara.b@simkas.com', '$2a$10$...', '081234567892', 2, 2, 1, true);

   -- MAHASISWA
   INSERT INTO users (nim, nama, email, password, phone, role_id, kelas_id, angkatan_id, aktif)
   VALUES 
   ('210103', 'Mahasiswa 1', 'mhs1@simkas.com', '$2a$10$...', '081234567893', 3, 1, 1, true),
   ('210104', 'Mahasiswa 2', 'mhs2@simkas.com', '$2a$10$...', '081234567894', 3, 1, 1, true),
   ('210105', 'Mahasiswa 3', 'mhs3@simkas.com', '$2a$10$...', '081234567895', 3, 2, 1, true);
   ```

   **Note:** Replace `$2a$10$...` dengan bcrypt hash dari password (gunakan bcrypt generator online)

4. **Edit application.properties**
   ```properties
   # Sesuaikan dengan Laragon config kamu
   spring.datasource.url=jdbc:mysql://localhost:3306/simkas?useSSL=false&serverTimezone=Asia/Jakarta
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   ```

5. **Run Application**
   - IntelliJ: Tekan `Shift + F10` atau click Run button
   - Backend akan jalan di `http://192.168.1.5:8080`
   - Verify Swagger UI: `http://192.168.1.5:8080/swagger-ui/index.html`

### Android Setup (Android Studio)

1. **Open Project**
   ```
   d:\Amel\Kuliah\Semester 5\Pemrograman Platform Khusus\simkas\SimkasApp
   ```

2. **Update RetrofitClient.kt**
   ```kotlin
   private const val BASE_URL = "http://192.168.1.5:8080/"
   ```

3. **Build & Run**
   - Android Studio: Tekan `Shift + F10`
   - Select emulator atau connected device
   - App akan start di WelcomeScreen

### Testing Credentials

| Username | Password | Role |
|----------|----------|------|
| 999001 | admin123 | ADMIN_ANGKATAN |
| 210101 | bendahara123 | BENDAHARA_KELAS (A) |
| 210102 | bendahara123 | BENDAHARA_KELAS (B) |
| 210103 | mhs123 | ANGGOTA |
| 210104 | mhs123 | ANGGOTA |
| 210105 | mhs123 | ANGGOTA |

### Testing Workflow

#### 1. Test Login (Any User)
1. Buka app
2. Click "Belum punya akun? Daftar" → Register baru sebagai ANGGOTA
3. Atau login dengan credentials di atas

#### 2. Test Admin Angkatan Flow
1. Login as Admin Angkatan (999001)
2. Dashboard: Lihat statistik angkatan
3. Klik "Buat Tempat Penarikan" → Create Kategori
   - Nama: "Kas Maret 2025"
   - Keterangan: "Penarikan kas bulanan"
   - Submit
4. List: Seharusnya bisa lihat kategori yang baru dibuat
5. Lihat History Transaksi

#### 3. Test Bendahara Kelas Flow
1. Login as Bendahara Kelas (210101)
2. Dashboard: Lihat statistik kelas A
3. Klik "+" → Create Kategori untuk Kelas A
   - Nama: "Kas Buku Tulis Maret"
   - Keterangan: "Untuk pembelian alat tulis"
   - Submit
4. List Wadah: Should see:
   - Kategori ANGKATAN (untuk dia setor ke admin)
   - Kategori KELAS milik dia sendiri (untuk kelolanya)
5. Klik Kategori ANGKATAN → Form setor
6. Edit Profil: Add/update kelas dan angkatan

#### 4. Test Mahasiswa Flow
1. Login as Mahasiswa (210103)
2. Dashboard: Lihat info dasar
3. Setor: Lihat kategori KELAS milik kelasnya (buatan bendahara A)
   - Click kategori → Form bayar
   - Input nominal, keterangan, bukti bayar
   - Submit
4. Riwayat: Lihat transaksi yang baru di-submit
5. Edit Profil: Pastikan kelas dan angkatan terisi
6. Logout: Clear SharedPreferences, kembali ke welcome

#### 5. Test File Upload
1. Upload bukti bayar (image file)
2. Check folder: `simkas/uploads/bukti-bayar/`
3. Verify file ada & namanya unique (UUID)

---

## 📞 TROUBLESHOOTING

### Android → Backend Connection Failed
**Error:** "koneksi error" / "failed to connect"

**Check:**
1. Pastikan Backend running: `http://192.168.1.5:8080/swagger-ui/`
2. Pastikan IP di RetrofitClient sama dengan IP laptop
3. Check firewall: Buka port 8080
4. Check network: HP dan laptop harus 1 jaringan WiFi (atau USB debug)
5. Logcat: Lihat pesan error detail

### Login Gagal (401)
**Error:** "Login Gagal! Cek kredensial"

**Check:**
1. NIM/Email & password benar?
2. User exist di database? (check via MySQL)
3. JWT token generate ok? (Cek backend log)
4. Password sudah di-bcrypt? (bukan plaintext)

### Token Expired
**Error:** "Unauthorized" setelah beberapa jam

**Solution:** 
- Implement token refresh endpoint
- Atau extend JWT expiration: `jwt.expiration=604800000` (7 days)

### File Upload 413 (Payload Too Large)
**Error:** "gagal upload file"

**Fix:** Add ke application.properties:
```properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### Database Connection Refused
**Error:** "Tidak bisa connect ke MySQL"

**Check:**
1. Laragon running? Start services
2. MySQL port 3306? Check di Laragon settings
3. Database `simkas` ada?
4. Username/password benar di application.properties?

---

## 🎓 SUMMARY

### What's Working ✅
1. Authentication (Login/Register)
2. JWT Token generation
3. Role-based routing
4. Database entities & relationships
5. Multipart file upload (struktur)
6. Dashboard data fetching
7. Basic CRUD operations

### What Needs Fixing 🔧
1. IP Address (10.100.162.8 → 192.168.1.5)
2. Bearer Token handling (add interceptor)
3. Kategori nominal field (add to entity & model)
4. Date handling (hardcoded → dynamic)
5. User ID tracking (SharedPreferences)
6. Error handling & logging
7. Input validation
8. Session management (logout)

### Priority Order
1. **TODAY:** Fix IP, Bearer token, Kategori nominal
2. **TOMORROW:** Test all flows end-to-end
3. **WEEK 2:** Add validation, error handling, logging
4. **WEEK 3:** Performance optimization, UI polish

---

**Last Updated:** 12 Januari 2026  
**Version:** 1.0 (Analysis Complete)  
**Status:** Ready for Implementation ✅
