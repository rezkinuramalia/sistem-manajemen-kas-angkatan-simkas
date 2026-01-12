# ✅ CHECKLIST IMPLEMENTASI SIMKAS - PHASE 1 (CRITICAL)

**Status:** Ready to Implement  
**Tanggal:** 12 Januari 2026  
**Version:** 1.0

---

## 📋 PERUBAHAN YANG SUDAH DILAKUKAN ✅

### Backend (Spring Boot)

- ✅ **AuthResponse.java** - Tambah field `id` dan `role`
- ✅ **AuthController.java** - Update login endpoint untuk return user id & role
- ✅ **Kategori.java** - Tambah field `nominal` (BigDecimal)
- ✅ **KategoriDto.java** - Tambah field `nominal`
- ✅ **KategoriRequest.java** - Tambah field `nominal`

### Android (Jetpack Compose)

- ✅ **RetrofitClient.kt** - Update IP ke 192.168.1.5 + Bearer Token Interceptor
- ✅ **Models.kt** - Tambah field:
  - `LoginResponse.id`
  - `Kategori.nominal`
  - `KategoriRequest.nominal`
  - `TransaksiRequest.buktiBayar`
  - Implement `Serializable` di semua response models
- ✅ **LoginScreen.kt** - Simpan `ID_USER` ke SharedPreferences
- ✅ **UploadScreen.kt** - Dynamic date handling (LocalDate.now())

### Database

- ✅ **DATABASE_SETUP.sql** - Script lengkap setup & seed data

---

## 🔧 LANGKAH IMPLEMENTASI YANG HARUS DILAKUKAN

### STEP 1: Database Setup (5-10 menit)

**Di Laragon / MySQL Workbench:**

```bash
# 1. Buka Laragon Terminal atau MySQL Workbench
# 2. Jalankan semua SQL di file DATABASE_SETUP.sql

# Via Command Line:
mysql -u root -p < D:\...\DATABASE_SETUP.sql

# Atau via MySQL Workbench:
# - Open DATABASE_SETUP.sql
# - Execute all (Ctrl + Shift + Enter)

# 3. Verify:
SELECT * FROM roles;
SELECT * FROM users LIMIT 5;
SELECT * FROM angkatan;
```

**Checklist:**
- [ ] Database `simkas` created
- [ ] All tables created (roles, users, kelas, angkatan, kategori)
- [ ] 3 roles inserted (ADMIN_ANGKATAN, BENDAHARA_KELAS, ANGGOTA)
- [ ] 7 test users inserted
- [ ] Sample kategori inserted
- [ ] Verify dengan SELECT queries di atas

---

### STEP 2: Backend Configuration (10-15 menit)

**File: `simkas/src/main/resources/application.properties`**

✅ Already checked - pastikan konfigurasi:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/simkas?useSSL=false&serverTimezone=Asia/Jakarta
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# JWT  
jwt.secret=MySuperSecretKeyForJWT123456
jwt.expiration=86400000

# Server
server.port=8080

# File Upload Size
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

**Checklist:**
- [ ] MySQL URL correct
- [ ] Username/password correct (test connection)
- [ ] JWT secret configured
- [ ] File upload size limits set
- [ ] Server port = 8080

**Di IntelliJ IDEA:**

```
1. Open simkas project
2. Go to Run > Edit Configurations
3. Select "simkas" (Spring Boot)
4. Verify VM options & environment variables
5. Click "Run" (Shift + F10) atau klik tombol Run
6. Wait for "Started SimKasApplication in X seconds"
7. Check console: "Tomcat started on port(s): 8080"
```

**Checklist:**
- [ ] Project compiled without error
- [ ] Backend running on port 8080
- [ ] Database connection successful (check logs)
- [ ] Swagger UI accessible: http://192.168.1.5:8080/swagger-ui/index.html

---

### STEP 3: Android Configuration (10-15 menit)

**File: `SimkasApp/app/src/main/java/.../api/RetrofitClient.kt`**

✅ Already updated with:
- IP: 192.168.1.5
- Bearer Token Interceptor

**File: `SimkasApp/app/src/main/java/.../models/Models.kt`**

✅ Already updated with all required fields

**File: `SimkasApp/build.gradle.kts`**

Pastikan dependency sudah ada:

```kotlin
dependencies {
    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    // ... (other compose dependencies)
}
```

**Checklist:**
- [ ] Retrofit dependency added
- [ ] OkHttp dependency added
- [ ] Gson dependency added
- [ ] build.gradle.kts synced

**Di Android Studio:**

```
1. Open SimkasApp project
2. Go to File > Sync with Gradle Files
3. Wait for sync complete
4. Select emulator atau connected device
5. Click Run (Shift + F10)
6. Wait for app to launch
```

**Checklist:**
- [ ] Project compiled without error
- [ ] App launched on emulator/device
- [ ] WelcomeScreen visible
- [ ] No network error di logcat (yet)

---

### STEP 4: Test Login Flow (10 menit)

**Di Android App:**

```
1. Click "Login"
2. Enter credentials:
   - Username: 999001 (Admin Angkatan)
   - Password: admin123
3. Click "MASUK"
```

**Expected Result:**

✅ Login successful  
✅ Redirect to Dashboard  
✅ SharedPreferences save:
- TOKEN: (JWT token dari backend)
- ROLE: ADMIN_ANGKATAN
- ID_USER: 1

**Checklist:**
- [ ] No "koneksi error" Toast
- [ ] No network timeout
- [ ] Token received from backend
- [ ] Redirect to dashboard successful
- [ ] Check Logcat for any errors

**If Login Failed:**

```
Error: "koneksi error" atau "failed to connect"
→ Check:
  1. Backend running? (http://192.168.1.5:8080/swagger-ui/)
  2. IP correct di RetrofitClient.kt?
  3. Laptop & phone/emulator same network?
  4. Firewall blocking port 8080?
  5. Check Logcat: adb logcat | grep "API_ERROR"
```

---

### STEP 5: Test Database Connectivity (5 menit)

**Via Postman/Insomnia:**

```
POST http://192.168.1.5:8080/api/auth/login
Content-Type: application/json

{
  "username": "999001",
  "password": "admin123"
}

Expected Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "999001",
  "id": 1,
  "role": "ADMIN_ANGKATAN"
}
```

**Checklist:**
- [ ] Status 200 OK
- [ ] Token received
- [ ] User ID returned
- [ ] Role returned

---

### STEP 6: Test Role-Based Dashboard (10 menit)

**Scenario 1: Admin Angkatan (999001)**

```
1. Login as 999001 / admin123
2. Dashboard should show:
   ✅ "Statistik Angkatan" title
   ✅ Saldo Angkatan card
   ✅ Pemasukan & Pengeluaran cards
   ✅ "Buat Tempat Penarikan" button
3. Click button → CreateKategoriScreen should appear
4. Create test kategori:
   - Nama: "Kas Maret 2025"
   - Keterangan: "Penarikan kas bulanan"
   - Nominal: 100000
   - Submit
5. Go back to dashboard
6. Check WadahListScreen: Should see created kategori
```

**Checklist:**
- [ ] Dashboard load successfully
- [ ] Correct dashboard variant for admin role
- [ ] Create kategori successful
- [ ] Kategori appear in list
- [ ] Nominal field working

**Scenario 2: Bendahara Kelas (210101)**

```
1. Logout (SharedPreferences clear)
2. Login as 210101 / bendahara123
3. Dashboard should show:
   ✅ "Statistik Kelas" title (not Angkatan)
   ✅ Kelas name (Kelas A)
   ✅ Different layout than Admin
4. Check menu: Should have both ANGKATAN & KELAS wadah
```

**Checklist:**
- [ ] Different dashboard for bendahara
- [ ] Role-based filtering working
- [ ] Can view both ANGKATAN & KELAS level kategori

**Scenario 3: Mahasiswa (210103)**

```
1. Logout
2. Login as 210103 / mhs123
3. Dashboard should show:
   ✅ Basic info
   ✅ "Setor" menu (or Upload button)
4. Click Setor → UploadScreen
5. Test upload:
   - Select kategori
   - Input nominal: 50000
   - Select image file
   - Submit
6. Check History: Should see submitted transaksi
```

**Checklist:**
- [ ] Mahasiswa dashboard layout correct
- [ ] Can only see KELAS level kategori (not ANGKATAN)
- [ ] Upload form working
- [ ] File upload successful
- [ ] History transaksi saved

---

### STEP 7: Test File Upload (5 menit)

**In UploadScreen:**

```
1. Select kategori: "Kas Kelas A - Buku Tulis"
2. Input nominal: 50000
3. Input keterangan: "Bukti bayar kas"
4. Click "Pilih File" → Select JPG/PNG image
5. Click "UPLOAD BUKTI BAYAR"
```

**Expected:**

✅ File uploaded successfully  
✅ Toast: "Berhasil Upload Bukti!"  
✅ File saved in `simkas/uploads/bukti-bayar/` folder

**Check file:**

```bash
# Via Command Line:
dir D:\...\simkas\uploads\bukti-bayar\
# Should see: UUID_bukti.jpg (e.g., "f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg")
```

**Checklist:**
- [ ] File upload dialog works
- [ ] File successfully uploaded to backend
- [ ] File saved with UUID name
- [ ] Database transaksi record created
- [ ] No 413 (Payload Too Large) error

---

### STEP 8: Test API Endpoint (Optional - Via Postman)

```
1. Test Login
   POST /api/auth/login
   Body: {"username": "999001", "password": "admin123"}
   Expected: 200 OK with token & id

2. Test Get Profile
   GET /api/users/profile
   Header: Authorization: Bearer {token}
   Expected: 200 OK with user details

3. Test Get Kategori
   GET /api/master/kategori
   Header: Authorization: Bearer {token}
   Expected: 200 OK with list of kategori

4. Test Create Kategori
   POST /api/master/kategori
   Header: Authorization: Bearer {token}
   Body: {"nama": "Test", "keterangan": "Test", "nominal": 50000}
   Expected: 201 CREATED with kategori object

5. Test Create Transaksi (Multipart)
   POST /api/transaksi
   Header: Authorization: Bearer {token}
   Body: Multipart form-data
   - Part "data": JSON {"idUser": 3, "idKategori": 1, ...}
   - Part "file": image file
   Expected: 200 OK with transaksi response
```

**Checklist:**
- [ ] All endpoints respond correctly
- [ ] Authorization header required (except /auth/*)
- [ ] Multipart upload works
- [ ] Database entries created

---

## 🐛 TROUBLESHOOTING

### Problem: "koneksi error" saat login

**Solutions:**
1. Verify backend running: Check `http://192.168.1.5:8080/swagger-ui/`
2. Check IP di RetrofitClient.kt: Should be `192.168.1.5`
3. Check same network: Laptop & device harus 1 WiFi
4. Check firewall: Buka port 8080
5. Check Logcat: `adb logcat | grep "IOException"`

### Problem: Login gagal (401 Unauthorized)

**Solutions:**
1. Check username/password di database: `SELECT * FROM users WHERE nim = '999001'`
2. Check password hash: Pastikan bcrypt hash benar
3. Check user aktif: `aktif = true`
4. Check role exists: `SELECT * FROM roles WHERE id = user.role_id`

### Problem: Kategori nominal tidak muncul

**Solutions:**
1. Check database: `ALTER TABLE kategori ADD COLUMN nominal DECIMAL(12,2) NOT NULL;`
2. Restart backend: Hibernate DDL harus update
3. Check Models.kt: Pastikan `nominal: Double` ada

### Problem: File upload 413 (Payload Too Large)

**Solutions:**
1. Add ke application.properties:
   ```properties
   spring.servlet.multipart.max-file-size=50MB
   spring.servlet.multipart.max-request-size=50MB
   ```
2. Restart backend

### Problem: Profile edit tidak bisa save kelas/angkatan

**Solutions:**
1. Check endpoint: `/api/users/profile` PUT method
2. Check request body: `UserProfileUpdateRequest` DTO
3. Check token: Pastikan Bearer token included

---

## ✅ FINAL VERIFICATION CHECKLIST

- [ ] Database setup complete dengan semua seed data
- [ ] Backend running di port 8080 tanpa error
- [ ] Android app compiled & running
- [ ] Login successful dengan credentials dari database
- [ ] All 3 roles dapat login & see different dashboards
- [ ] Kategori creation working (Admin & Bendahara)
- [ ] Kategori list filtering working (per role)
- [ ] File upload working (bukti bayar tersimpan)
- [ ] History transaksi showing correctly
- [ ] Profile edit can update kelas & angkatan
- [ ] Logout clear SharedPreferences & redirect to login
- [ ] No network errors di logcat (except auth errors)
- [ ] No database constraint errors
- [ ] No NPE (NullPointerException) di app

---

## 📞 NEXT STEPS (PHASE 2 & 3)

After Phase 1 complete:

### PHASE 2 (HIGH PRIORITY)
- [ ] Add error message display (not just Toast)
- [ ] Add loading indicator di setiap network call
- [ ] Add session timeout handling
- [ ] Add input validation (nominal > 0, email format, etc)
- [ ] Add image compression sebelum upload
- [ ] Add retry mechanism untuk failed uploads

### PHASE 3 (MEDIUM PRIORITY)
- [ ] Add transaction history filtering (by date, status)
- [ ] Add dashboard data refresh pull-down
- [ ] Add PDF export untuk history
- [ ] Add push notification untuk new transaksi
- [ ] Add analytics dashboard
- [ ] Add user activity logging

---

**Status:** ✅ Ready for Testing  
**Last Updated:** 12 Januari 2026
