# HAJIRI BOOK – SMART ATTENDANCE SYSTEM

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![TensorFlow Lite](https://img.shields.io/badge/AI-TensorFlow%20Lite-orange.svg)](https://www.tensorflow.org/lite)
[![PHP](https://img.shields.io/badge/Backend-PHP%208.1-indigo.svg)](https://www.php.net)
[![MySQL](https://img.shields.io/badge/Database-MySQL%208.0-teal.svg)](https://www.mysql.com)

**Hajiri Book** is a complete, production-quality, offline-first school attendance management system. It combines on-device AI face recognition (MobileFaceNet), CameraX live tracking, anti-spoofing liveness detection, Room local database persistence, and bidirectional synchronization with a PHP REST API and MySQL server.

---

## 📱 Visual Design System

All 15 screens strictly conform to the **Hajiri Book** brand design language:
- **Primary Navy Blue**: `#082B63`
- **Secondary Blue**: `#1769CE`
- **Primary Green**: `#16A05A`
- **Light Background**: `#F4F7FC`
- **Card Background**: `#FFFFFF`
- **Bottom Navigation**: Home, Add +, Scan (Elevated Green Center), Reports, Settings

---

## 🗂️ Project Directory Structure

```
hajiri-book/
├── android/                         # Complete Android Studio Project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/pkstudy/hajiribook/
│   │   │   │   ├── ai/              # On-device AI & Biometrics
│   │   │   │   │   ├── detector/    # TFLite Face Detector (Bounding Box)
│   │   │   │   │   ├── embedding/   # MobileFaceNet (192D Embedding)
│   │   │   │   │   ├── liveness/    # Anti-spoofing & Liveness Detection
│   │   │   │   │   ├── recognition/ # Cosine Similarity Matching Engine
│   │   │   │   │   └── tracker/     # Multi-Face Tracking & Cooldown
│   │   │   │   ├── camera/          # CameraX Analyzer (STRATEGY_KEEP_ONLY_LATEST)
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/       # Room Database (Entities, DAOs, Converters)
│   │   │   │   │   ├── remote/      # Retrofit 2 API, DTOs, AuthInterceptor
│   │   │   │   │   └── repository/  # Offline-First HajiriRepository
│   │   │   │   ├── security/        # AES-256-GCM Encrypted Local Backup & Restore
│   │   │   │   ├── speech/          # TextToSpeech Manager (Hindi/English Announcements)
│   │   │   │   ├── sync/            # WorkManager Background Synchronization
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/  # TopBar, BottomNav, StatCard, Drawer
│   │   │   │   │   ├── navigation/  # NavHost & ModalNavigationDrawer
│   │   │   │   │   ├── screens/     # 15 Complete Screens (Compose UI)
│   │   │   │   │   └── theme/       # Color, Type, Theme
│   │   │   │   ├── viewmodel/       # MVVM StateFlow ViewModels
│   │   │   │   ├── HajiriApp.kt     # Application Dependency Container
│   │   │   │   └── MainActivity.kt  # Edge-to-Edge Compose Activity
│   │   │   ├── res/                 # Colors, Strings, Themes, XML Security
│   │   │   └── AndroidManifest.xml  # Permissions (Camera, Network, Storage)
│   │   ├── src/test/                # Unit Tests (AI, Tracking, Similarity)
│   │   └── build.gradle.kts         # App Gradle Build Script
│   ├── gradle/libs.versions.toml    # Modern Version Catalog
│   └── settings.gradle.kts          # Root Settings
│
├── backend/                         # Complete PHP REST API
│   ├── config/                      # Database (PDO) & CORS Headers
│   ├── middleware/                  # JWT Authentication & RBAC Middleware
│   ├── auth/                        # login.php (bcrypt password verification)
│   ├── students/                    # add_student.php, get_students.php
│   ├── face/                        # register_face.php (192D template store)
│   ├── attendance/                  # mark_attendance.php (duplicate prevention)
│   ├── sync/                        # sync_push.php, sync_pull.php
│   ├── reports/                     # daily_report.php (aggregate stats)
│   └── index.php                    # API Healthcheck & Documentation
│
├── database/
│   └── hajiri_book.sql              # Production MySQL Schema & Seed Data
│
└── docs/
    ├── SETUP_GUIDE.md               # Complete Setup & Installation Steps
    ├── AI_MODELS_GUIDE.md           # Model Specs, Normalization & Math
    └── ARCHITECTURE.md              # Clean Architecture & Flow Diagrams
```

---

## ⚡ Key Architectural Highlights

1. **Strict Offline-First**: Every attendance record and registered student is saved into the local Room database first. The app operates reliably with or without internet.
2. **True AI Vector Recognition**: Uses 192-dimensional numerical face embeddings extracted via MobileFaceNet and compares them with cosine similarity against a configurable confidence threshold (`0.72`).
3. **Anti-Spoofing & Liveness**: Analyzes active challenges (eye blink, head angle turn) and natural micro-movements to reject photo prints and screen replay attacks.
4. **Multi-Face Tracking & Duplicate Prevention**: Tracks multiple simultaneous faces across frames using IoU tracking with an 8-second cooldown to prevent spamming records and voice announcements.
5. **Non-Blocking Bilingual TextToSpeech**: Announces recognized students in English or Hindi without stalling the camera frame pipeline.
6. **Encrypted Local Backups**: Exports Room database using AES-256 in GCM mode with password protection.
7. **Background Sync with WorkManager**: Enqueues offline changes into a `sync_queue` table and automatically uploads them with exponential backoff when connectivity returns.

---

## 🚀 Quick Start
Refer to [SETUP_GUIDE.md](docs/SETUP_GUIDE.md) for full setup instructions.
