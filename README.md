<p align="center">
  <img src="docs/nurapp-logo.svg" alt="NurApp logo" width="180" />
</p>

<h1 align="center">NurApp</h1>

<p align="center">
  Islamic Android app for Qur'an reading, adzan reminders, hadith search, Ramadan tools, qibla access, and in-app updates.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-0F5238?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-16423C?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Compose" />
  <img src="https://img.shields.io/badge/Stack-Room%20%7C%20Retrofit%20%7C%20Hilt-D6B66A?style=for-the-badge&logo=google&logoColor=1A1A1A" alt="Room Retrofit Hilt" />
  <img src="https://img.shields.io/badge/Audio-Media3%20Offline-4B6359?style=for-the-badge&logo=android&logoColor=white" alt="Media3 Offline" />
</p>

<p align="center">
  <a href="https://github.com/saferill/Nur-App/releases/tag/v1.3.0">
    <img src="https://img.shields.io/badge/Latest%20Release-v1.3.0-0F5238?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v1.3.0" />
  </a>
  <a href="https://github.com/saferill/Nur-App/releases/download/v1.3.0/NurApp-v1.3.0.apk">
    <img src="https://img.shields.io/badge/Download%20APK-NurApp%20v1.3.0-D6B66A?style=for-the-badge&logo=android&logoColor=1A1A1A" alt="Download NurApp v1.3.0 APK" />
  </a>
</p>

## Overview

NurApp adalah arah produk terbaru dari repository ini. Nama aplikasi untuk user sudah memakai `NurApp`, sementara package Android masih tetap `com.sajda.app` agar signature, update path, dan kontinuitas install lama tidak putus.

Repository GitHub juga sekarang memakai lokasi baru:

- Repo aktif: `https://github.com/saferill/Nur-App`
- Package Android: `com.sajda.app`

Fokus aplikasi ini adalah pengalaman ibadah harian yang tetap usable saat offline, lalu menyegarkan data penting seperti hadith, doa, jadwal sholat, lokasi, dan update APK ketika koneksi tersedia.

## Highlights

### Navigasi utama

- 6 tab bawah: `Beranda`, `Qur'an`, `Adzan`, `Hadist`, `Ramadhan`, `Pengaturan`
- Kalender dan shortcut kiblat ditempatkan di area beranda
- UI utama berbasis single-activity dengan Compose dan overlay flow
- Splash screen sudah disederhanakan menjadi satu alur launch

### Beranda

- Ringkasan salam, lokasi, tanggal Hijriah atau Masehi
- Kartu waktu sholat berikutnya dengan countdown
- Ringkasan jadwal sholat hari ini
- Last read, murattal cepat, dan ayat harian
- Akses kalender dan kiblat dari header

### Qur'an

- Database Qur'an lokal dari asset bawaan
- Tampilan surah dengan Arab, terjemahan, bookmark, dan progress baca
- Tafsir full, bukan ringkasan singkat
- Pilihan banyak qari
- Download audio per surah untuk kebutuhan offline
- Playback dengan mini player dan service background

### Adzan

- Alarm exact untuk Subuh, Dzuhur, Ashar, Maghrib, dan Isya
- Jalur alarm diperkuat dengan receiver, fallback alert, dan reschedule setelah reboot
- Pemisahan suara adzan reguler dan adzan Subuh
- Lokasi adzan bisa update otomatis dari GPS
- Sinkron jadwal ulang jika kota berubah

### Hadist

- Pencarian hadist berdasarkan kitab, kata kunci, atau nomor
- Tipografi Arab diperbesar agar lebih nyaman dibaca
- Integrasi hadith API untuk pencarian konten hadist

### Ramadhan

- Ringkasan hitung mundur Ramadhan
- Jadwal imsak dan iftar
- Konten doa berbuka dan sahur
- Konten amalan Ramadhan

### Pengaturan

- Pengaturan adzan, qari, audio, bahasa, tampilan, dan lokasi
- Bahasa aplikasi dan format tanggal
- Preferensi audio Qur'an dan pengingat adzan

### Auto update

- Cek rilis GitHub saat aplikasi dibuka
- Download APK update di background via WorkManager
- Banner progres download di bawah layar
- Install APK langsung via `FileProvider`

## Fitur Teknis Penting

- Auto-update lokasi adzan memakai `FusedLocationProviderClient`
- Reverse geocode ke nama kota dengan `Geocoder`
- Background check lokasi setiap 3 jam via `WorkManager`
- Compare kota lama vs kota baru sebelum fetch jadwal ulang
- Reschedule semua alarm adzan saat kota berubah
- Auto update APK dari GitHub Releases
- Penyimpanan preferensi dan cache ringan dengan `DataStore` dan `SharedPreferences`

## Tech Stack

| Layer | Stack |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 + ViewBinding host for update banner |
| Architecture | MVVM + Repository Pattern |
| Dependency Injection | Hilt |
| Local Database | Room |
| Preferences | DataStore + SharedPreferences |
| Networking | Retrofit2 + Gson + OkHttp |
| Background | WorkManager + AlarmManager + Foreground Service |
| Location | Google Play Services Fused Location |
| Audio | Media3 / ExoPlayer |

## Data Sources

NurApp memakai kombinasi local data dan public endpoints. Source yang saat ini dipakai atau sudah disiapkan:

- `AlQuran Cloud`
- `EQuran.id`
- `MyQuran`
- `Hadith Gading`
- `Quran.com` tafsir endpoints
- `GitHub Releases API` untuk update APK

Catatan source tambahan ada di [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md).

## Build Requirements

- Android Studio
- Android SDK untuk `compileSdk = 36`
- JDK 17 atau JDK 21
- File `keystore.properties` lama untuk build release
- `local.properties` jika ingin override endpoint atau API-related values

## Local Configuration

Project membaca nilai opsional berikut dari `local.properties`:

```properties
hadith.api.key=
hadith.api.baseUrl=https://hadithapi.com/public/api
dua.content.url=https://raw.githubusercontent.com/wafaaelmaandy/Hisn-Muslim-Json/master/hisnmuslim.json
```

Kalau tidak diisi, project memakai fallback default dari [app/build.gradle.kts](app/build.gradle.kts).

## Build

### Debug build

```bash
./gradlew assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release build

Release build wajib memakai keystore lama dari `keystore.properties`.

```bash
./gradlew assembleRelease
```

Output:

```text
app/build/outputs/apk/release/NurApp-v1.3.0.apk
```

## Latest Release

- Release page: [v1.3.0](https://github.com/saferill/Nur-App/releases/tag/v1.3.0)
- Direct APK: [NurApp-v1.3.0.apk](https://github.com/saferill/Nur-App/releases/download/v1.3.0/NurApp-v1.3.0.apk)

## Project Docs

- Build notes: [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)
- Contribution guide: [CONTRIBUTING.md](CONTRIBUTING.md)
- Delivery checklist: [DELIVERY_CHECKLIST.md](DELIVERY_CHECKLIST.md)
- Content source notes: [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md)

## Project Structure

```text
app/src/main/
|- java/com/sajda/app/
|  |- data/
|  |  |- api/
|  |  |- local/
|  |  |- model/
|  |  |- remote/
|  |  `- repository/
|  |- di/
|  |- domain/model/
|  |- service/
|  |- ui/
|  |  |- component/
|  |  |- screen/
|  |  |- theme/
|  |  `- viewmodel/
|  |- util/
|  |- utils/
|  |- widget/
|  |- worker/
|  |- MainActivity.kt
|  `- SajdaApplication.kt
|- assets/
|- res/
`- AndroidManifest.xml
```

## Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | Fetch API content and check updates |
| `ACCESS_FINE_LOCATION` | GPS-based prayer location |
| `ACCESS_COARSE_LOCATION` | Fallback location access |
| `POST_NOTIFICATIONS` | Adzan and update notifications |
| `FOREGROUND_SERVICE` | Audio and adzan foreground service |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background media playback |
| `SCHEDULE_EXACT_ALARM` | Exact prayer alarm scheduling |
| `USE_EXACT_ALARM` | Better exact alarm behavior on supported devices |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Better reminder reliability on aggressive OEM devices |
| `REQUEST_INSTALL_PACKAGES` | Install downloaded APK updates |
| `WRITE_EXTERNAL_STORAGE` | Legacy external file compatibility on older Android |
| `RECEIVE_BOOT_COMPLETED` | Repair schedules after reboot |
| `WAKE_LOCK` | Keep alarm path stable during trigger |
| `USE_FULL_SCREEN_INTENT` | More visible urgent adzan flow when needed |

## Notes

- Release signature dijaga tetap sama lewat `keystore.properties`.
- Nama brand user-facing adalah `NurApp`, tapi beberapa identifier internal masih memakai nama lama demi kompatibilitas.
- Sebagian fallback terjemahan UI masih mengikuti locale default kalau string khusus bahasa tersebut belum tersedia penuh.

## Contributing

Panduan kontribusi ada di [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Repository ini belum memiliki file license final.
