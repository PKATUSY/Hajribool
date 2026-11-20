# HAJIRI BOOK – SETUP & DEPLOYMENT GUIDE

## 1. Project Overview
**Hajiri Book** is a smart school attendance management application developed in Kotlin using Jetpack Compose, CameraX, on-device TensorFlow Lite (MobileFaceNet), offline Room database, and bidirectional synchronization with a PHP & MySQL backend.

---

## 2. Prerequisites
- **Android Studio**: Ladybug / Meerkat (2024.2+) or newer
- **Android SDK**: Compile SDK 35, Min SDK 24 (Android 7.0+)
- **JDK**: OpenJDK 17 or higher
- **Web Server**: Apache / Nginx with PHP 8.1+ & PDO MySQL
- **Database**: MySQL 8.0+ or MariaDB 10.6+

---

## 3. PHP & MySQL Backend Setup

### Step 3.1: Import MySQL Database
1. Open your MySQL client (e.g. phpMyAdmin, MySQL Workbench, or CLI):
   ```bash
   mysql -u root -p -e "CREATE DATABASE hajiri_book CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   mysql -u root -p hajiri_book < database/hajiri_book.sql
   ```
2. Verify seeded records:
   - Users: `admin` (Password: `123456`), `madhurendra` (Password: `123456`)
   - School: `Central Model High School`
   - Sample Students: Rahul Kumar, Aman Kumar, Priya Kumari, Suman Kumar, Rohit Kumar

### Step 3.2: Configure PHP API
1. Place the `backend/` folder into your web root (e.g., `/var/www/html/hajiri-api` or `xampp/htdocs/hajiri-api`).
2. Open `backend/config/database.php` and verify credentials:
   ```php
   private $host = "localhost";
   private $db_name = "hajiri_book";
   private $username = "root";
   private $password = "";
   ```
3. Test API in browser or curl:
   ```bash
   curl http://localhost/hajiri-api/index.php
   ```
   Expected response:
   ```json
   {"status":"success","message":"Hajiri Book REST API v1.0.0 is operational", ...}
   ```

---

## 4. Android Studio Setup & Build

### Step 4.1: Import Project
1. Launch **Android Studio**.
2. Select **Open** and choose the `android/` directory inside this repository.
3. Allow Gradle to synchronize dependencies.

### Step 4.2: TFLite Face Recognition Models
1. Ensure the model files are placed in `android/app/src/main/assets/models/`:
   - `mobilefacenet.tflite` (112x112 input, 192-dim vector)
   - `face_detector.tflite` (Face detector model)
2. (Refer to `docs/AI_MODELS_GUIDE.md` for download links and pre-trained weights).

### Step 4.3: Running on Device / Emulator
1. Connect an Android smartphone or tablet (with USB debugging enabled) or launch an Android Emulator (API 34/35).
2. For Android Emulator accessing local host:
   - Base URL is pre-configured to `http://10.0.2.2/hajiri-api/api/`.
3. For a Physical Device on the same Wi-Fi:
   - Go to **Settings → Sync & Server (MySQL)** inside the app.
   - Enter your computer's local IP address (e.g. `http://192.168.1.100/hajiri-api/api/`).
4. Press **Run (Shift + F10)** to compile and install the APK.

---

## 5. Offline Operation & Testing
1. **Offline Mode**: Turn on Airplane mode on the test device.
2. The app continues to work without internet:
   - Shows existing students and classes.
   - Live face scanning works locally using on-device TFLite.
   - Attendance is marked and stored in the local Room database with status `PENDING`.
3. **Reconnection & Sync**: Turn Airplane mode off:
   - WorkManager automatically triggers background sync when network is detected.
   - Or tap **Sync Now** in the **Data Sync** screen to push pending records immediately.
