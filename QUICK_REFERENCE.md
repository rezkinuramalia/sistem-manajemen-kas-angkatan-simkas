# ⚡ QUICK REFERENCE - SIMKAS IMPLEMENTATION

**Last Updated:** 12 Januari 2026  
**Time to Complete Phase 1:** ~60-80 menit

---

## 🏃 QUICK START (5 STEPS)

### Step 1: Database Setup (10 min)
```bash
# Open MySQL Workbench or Terminal
mysql -u root -p < DATABASE_SETUP.sql

# Verify
mysql> USE simkas;
mysql> SELECT COUNT(*) FROM users;  # Should be 7
mysql> SELECT COUNT(*) FROM roles;  # Should be 3
```

### Step 2: Backend Setup (15 min)
```bash
# IntelliJ IDEA
1. Open: simkas/
2. Right-click project → Maven → Reload projects
3. Shift + F10 (Run)
4. Wait for "Tomcat started on port(s): 8080"
5. Verify: http://192.168.1.5:8080/swagger-ui/
```

### Step 3: Android Setup (15 min)
```bash
# Android Studio
1. Open: SimkasApp/
2. File → Sync with Gradle Files
3. Shift + F10 (Run)
4. Select emulator/device
5. Wait for app to launch
```

### Step 4: Test Login (5 min)
```
Credentials:
- Username: 999001
- Password: admin123

Expected: Dashboard appears (no "koneksi error")
```

### Step 5: Verify in Postman (5 min)
```bash
POST http://192.168.1.5:8080/api/auth/login
Content-Type: application/json

{
  "username": "999001",
  "password": "admin123"
}

# Expected Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "999001",
  "id": 1,
  "role": "ADMIN_ANGKATAN"
}
```

---

## 🔑 TEST CREDENTIALS

| Username | Password | Role | Kelas |
|----------|----------|------|-------|
| 999001 | admin123 | ADMIN_ANGKATAN | - |
| 210101 | bendahara123 | BENDAHARA_KELAS | A |
| 210102 | bendahara123 | BENDAHARA_KELAS | B |
| 210103 | mhs123 | ANGGOTA | A |
| 210104 | mhs123 | ANGGOTA | A |
| 210105 | mhs123 | ANGGOTA | B |
| 210106 | mhs123 | ANGGOTA | B |

---

## 🐛 COMMON ERRORS & FIXES

### Error: "koneksi error" atau "failed to connect"

```
❌ Problem: IP salah atau backend tidak running
✅ Fix:
   1. Check RetrofitClient.kt: BASE_URL = "http://192.168.1.5:8080/"
   2. Check backend running: http://192.168.1.5:8080/swagger-ui/
   3. Check same WiFi network
   4. Check firewall port 8080
```

### Error: "Login Gagal! Cek kredensial"

```
❌ Problem: User tidak ada di database atau password salah
✅ Fix:
   1. Verify user ada: mysql> SELECT * FROM users WHERE nim = '999001';
   2. Check password hash correct
   3. Verify role exists: mysql> SELECT * FROM roles;
```

### Error: "Gagal upload file"

```
❌ Problem: File size terlalu besar atau permission issue
✅ Fix:
   1. Check application.properties:
      spring.servlet.multipart.max-file-size=50MB
   2. Check folder permission: uploads/bukti-bayar/
   3. Test dengan file < 5MB dulu
```

### Error: "Gagal memproses gambar" di UploadScreen

```
❌ Problem: Image file corrupted atau format tidak support
✅ Fix:
   1. Use JPG or PNG files only
   2. File size < 5MB
   3. Check image format: file /path/to/image.jpg
```

### Error: "Unauthorized" (401)

```
❌ Problem: Token expired atau invalid
✅ Fix:
   1. Logout & login lagi (clear SharedPreferences)
   2. Check token dalam logcat
   3. Verify Authorization header: Bearer {token}
```

---

## 📱 TESTING WORKFLOW

### Admin Angkatan (999001)
```
1. Login → Dashboard (Statistik Angkatan)
2. Click "Buat Tempat Penarikan"
3. Input: Nama, Keterangan, Nominal
4. Submit → Success
5. View list → Should show kategori
6. Click kategori → Detail
7. Back → Dashboard → History (empty at first)
```

### Bendahara Kelas (210101)
```
1. Login → Dashboard (Statistik Kelas)
2. Create kategori KELAS level → Success
3. View list → See 2 kategori:
   - ANGKATAN level (untuk setor ke admin)
   - KELAS level (buatan dia)
4. Click ANGKATAN kategori → Form setor
5. Input nominal & keterangan
6. (Optional) Upload bukti → Submit
7. View History → Show submitted transaksi
```

### Mahasiswa (210103)
```
1. Login → Dashboard (Basic info)
2. View "Setor" menu
3. See KELAS kategori (only their class)
4. Click kategori → UploadScreen
5. Input: Nominal, Keterangan, Select Image
6. Click "UPLOAD BUKTI BAYAR"
7. Toast: "Berhasil Upload Bukti!"
8. View History → Show submitted transaksi
```

---

## 📊 API ENDPOINTS REFERENCE

### Authentication
```
POST /api/auth/login
POST /api/auth/register
```

### Profile
```
GET  /api/users/profile
PUT  /api/users/profile
```

### Master Data
```
GET  /api/master/kategori
POST /api/master/kategori
GET  /api/master/kelas
GET  /api/master/angkatan
```

### Dashboard
```
GET  /api/dashboard/kelas
GET  /api/dashboard/angkatan
```

### Transaksi
```
POST /api/transaksi (multipart)
GET  /api/transaksi/history
GET  /api/transaksi/kelas
GET  /api/transaksi/angkatan
```

---

## 🔧 DATABASE QUERIES

### Check Users
```sql
SELECT id, nim, nama, email, role_id, kelas_id, angkatan_id 
FROM users 
ORDER BY id;
```

### Check Roles
```sql
SELECT * FROM roles;
```

### Check Kategori
```sql
SELECT id, nama, level, nominal, id_kelas_pemilik 
FROM kategori 
ORDER BY created_at DESC;
```

### Check Transaksi
```sql
SELECT t.id, t.nominal, u.nim, k.nama, t.status_validasi 
FROM transaksi t
LEFT JOIN users u ON t.id_user = u.id
LEFT JOIN kategori k ON t.id_kategori = k.id
ORDER BY t.created_at DESC;
```

### Reset Database (if needed)
```sql
DROP DATABASE simkas;
CREATE DATABASE simkas DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
# Then run: mysql -u root -p < DATABASE_SETUP.sql
```

---

## 📁 IMPORTANT FILES

### Backend
```
simkas/
├── src/main/resources/application.properties   ← Database & JWT config
├── src/main/java/com/polstat/simkas/
│   ├── controller/AuthController.java          ← Login/Register
│   ├── entity/Kategori.java                    ← Wadah/Tempat Bayar
│   ├── entity/Transaksi.java                   ← Pembayaran record
│   └── dto/AuthResponse.java                   ← Login response
└── pom.xml                                      ← Dependencies
```

### Android
```
SimkasApp/
├── app/src/main/java/com/example/simkasapp/
│   ├── api/RetrofitClient.kt                   ← API config
│   ├── api/ApiService.kt                       ← Endpoints
│   ├── models/Models.kt                        ← Data classes
│   ├── screens/LoginScreen.kt                  ← Login UI
│   ├── screens/DashboardScreen.kt              ← Dashboard UI
│   └── screens/UploadScreen.kt                 ← File upload UI
└── build.gradle.kts                             ← Dependencies
```

### Database
```
DATABASE_SETUP.sql                               ← Complete setup script
```

### Documentation
```
ANALISIS_SIMKAS.md                              ← Detailed analysis
CHECKLIST_IMPLEMENTASI.md                       ← Step-by-step guide
RINGKASAN_ANALISIS.md                           ← Summary & overview
QUICK_REFERENCE.md (this file)                  ← Quick lookup
```

---

## 🚨 CRITICAL CHECKLIST

Before running:
- [ ] IP in RetrofitClient.kt = 192.168.1.5
- [ ] Backend properties: mysql localhost:3306, port 8080
- [ ] Database created: simkas
- [ ] Roles inserted: 3 roles
- [ ] Test users inserted: 7 users
- [ ] Kategori entity has nominal field
- [ ] AuthResponse has id & role fields
- [ ] LoginResponse model has id field
- [ ] Models have Serializable interface

After setup:
- [ ] Backend running: http://192.168.1.5:8080/swagger-ui/
- [ ] Android app compiled without error
- [ ] Login successful with 999001/admin123
- [ ] Dashboard displays correctly
- [ ] No "koneksi error" Toast
- [ ] Logcat shows no network errors

---

## 📞 TROUBLESHOOTING CHECKLIST

If something doesn't work:

1. **Check Logcat (Android)**
   ```bash
   adb logcat | grep "ERROR\|IOException\|API_ERROR"
   ```

2. **Check Backend Console**
   - Look for exceptions in IntelliJ console
   - Check database connection errors

3. **Check Database Connection**
   ```bash
   mysql -u root -p
   USE simkas;
   SHOW TABLES;
   SELECT COUNT(*) FROM users;
   ```

4. **Test API in Postman**
   ```
   POST http://192.168.1.5:8080/api/auth/login
   Body: {"username": "999001", "password": "admin123"}
   Expected: 200 OK with token
   ```

5. **Check Network**
   ```bash
   # Ping laptop dari device
   ping 192.168.1.5
   
   # Check port 8080
   netstat -an | grep 8080
   ```

---

## ⏱️ TIME ESTIMATES

| Task | Time | Notes |
|------|------|-------|
| Database Setup | 5-10 min | Run SQL script |
| Backend Compilation | 10-15 min | Maven install + build |
| Backend Startup | 5 min | Wait for Tomcat |
| Android Compilation | 10-15 min | Gradle sync + build |
| Android Startup | 5 min | Launch on emulator |
| Login Test | 2 min | Simple test |
| Dashboard Test | 5 min | Verify role-based display |
| Create Kategori Test | 3 min | Test creation |
| File Upload Test | 5 min | Test multipart upload |
| **Total (Phase 1)** | **60-80 min** | Complete setup + basic testing |

---

## 🎯 SUCCESS CRITERIA

Phase 1 adalah successful jika:

✅ Login works (no network error)  
✅ Dashboard displays per role  
✅ Kategori creation works  
✅ File upload successful  
✅ History transaksi shows  
✅ No critical errors di logcat  
✅ Database records created correctly  
✅ All 3 roles can login  

Jika semua ✅, Anda ready untuk:
- Phase 2: Error handling & validation
- Phase 3: Advanced features

---

**Keep this file handy while implementing! 🚀**

Last Updated: 12 Januari 2026
