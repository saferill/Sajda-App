<p align="center">
  <img src="docs/nurapp-logo.svg" alt="NurApp logo" width="180" />
</p>

<h1 align="center">NurApp</h1>

<p align="center">
  A modern Islamic Android app for Qur'an reading, adhan reminders, hadith search, Ramadan tools, qibla access, and safe in-app updates.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-0F5238?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-16423C?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Compose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%7C%20Repository-D6B66A?style=for-the-badge&logo=android&logoColor=1A1A1A" alt="Architecture" />
  <img src="https://img.shields.io/badge/Release-v1.4.0-0F5238?style=for-the-badge&logo=github&logoColor=white" alt="Release v1.4.0" />
</p>

<p align="center">
  <a href="https://github.com/saferill/Nur-App/releases/tag/v1.4.0">Release Notes</a>
  |
  <a href="https://github.com/saferill/Nur-App/releases/download/v1.4.0/NurApp-v1.4.0.apk">Download APK</a>
  |
  <a href="ROADMAP.md">Roadmap</a>
</p>

## Overview

NurApp adalah aplikasi Android Kotlin yang berfokus pada pengalaman ibadah harian yang rapi, cepat, dan tetap berguna saat offline. Fitur utamanya mencakup Qur'an, adhan, hadith, Ramadhan, kalender Islam, qibla, pengelolaan audio murattal, serta update APK langsung dari GitHub Releases.

Brand user-facing aplikasi adalah `NurApp`, sementara package Android tetap `com.sajda.app` untuk menjaga kompatibilitas instalasi lama, signature, dan jalur update.

## Current Status

Project ini aktif dikembangkan. Prioritas saat ini bukan menambah tab baru, tetapi memperkuat kualitas inti:

- adhan yang lebih andal di device nyata
- cleanup bahasa aplikasi dan resource locale
- UI yang lebih tenang dan tidak berantakan
- backup, restore, dan update flow yang lebih aman
- test coverage untuk flow kritis

## Core Features

### Qur'an

- Offline-first Qur'an data dari asset lokal
- Tampilan surah dan ayat dengan teks Arab, terjemahan, transliterasi, bookmark, dan last read
- Tafsir full screen
- Banyak pilihan qari
- Download audio per surah untuk kebutuhan offline
- Mini player dan background audio playback

### Adhan & Prayer Times

- Jadwal sholat harian dengan exact alarm
- Adhan terpisah untuk regular dan Subuh
- Reschedule alarm setelah reboot dan repair flow
- Auto-update lokasi adhan dari GPS
- Diagnosa adhan untuk cek kesiapan sistem
- Countdown ke waktu sholat berikutnya

### Hadith & Spiritual Content

- Pencarian hadith berdasarkan kitab, kata kunci, dan nomor
- Tipografi Arab yang lebih nyaman dibaca
- Doa harian dan konten spiritual tambahan
- Highlight hasil pencarian untuk membantu scanning cepat

### Ramadan, Calendar, and Qibla

- Mode Ramadhan dengan jadwal imsak dan iftar
- Doa berbuka dan sahur
- Kalender Islam dan kalender Masehi
- Akses qibla langsung dari home flow

### App Update & Local Data

- Cek versi terbaru dari GitHub Releases
- Download APK update via WorkManager
- Banner progres download di dalam aplikasi
- Install APK via FileProvider
- Backup dan restore data lokal inti

## Product Direction

Tujuan NurApp adalah menjadi aplikasi Muslim harian yang:

- cepat dibuka
- jelas dipakai
- tidak penuh elemen yang tidak penting
- tetap berguna walau koneksi tidak stabil
- bisa dipercaya untuk flow adhan

Roadmap pengembangan detail ada di [ROADMAP.md](ROADMAP.md).

## Tech Stack

| Area | Stack |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 + ViewBinding host |
| Architecture | MVVM + Repository Pattern |
| Dependency Injection | Hilt |
| Local Storage | Room + DataStore + SharedPreferences |
| Networking | Retrofit2 + Gson + OkHttp |
| Background Work | WorkManager + AlarmManager + Foreground Service |
| Audio | Media3 / ExoPlayer |
| Location | FusedLocationProviderClient + Geocoder |
| Update Delivery | GitHub Releases API + FileProvider |

## Data Sources

NurApp memakai kombinasi local assets dan public API:

- AlQuran Cloud
- EQuran.id
- MyQuran
- Hadith Gading
- Quran.com tafsir endpoints
- GitHub Releases API

Referensi source tambahan ada di [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md).

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

## Requirements

- Android Studio terbaru yang mendukung AGP project ini
- Android SDK dengan `compileSdk = 36`
- JDK 17 atau JDK 21
- `keystore.properties` lama untuk signed release
- `local.properties` jika ingin override endpoint atau API-related values

## Local Configuration

Project membaca nilai opsional berikut dari `local.properties`:

```properties
hadith.api.key=
hadith.api.baseUrl=https://hadithapi.com/public/api
dua.content.url=https://raw.githubusercontent.com/wafaaelmaandy/Hisn-Muslim-Json/master/hisnmuslim.json
```

Kalau tidak diisi, project memakai fallback default dari `app/build.gradle.kts`.

## Build

### Debug

```bash
./gradlew assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release

Release build wajib memakai keystore lama dari `keystore.properties`.

```bash
./gradlew assembleRelease
```

Output:

```text
app/build/outputs/apk/release/NurApp-v1.4.0.apk
```

## Latest Release

- Release page: [v1.4.0](https://github.com/saferill/Nur-App/releases/tag/v1.4.0)
- Direct APK: [NurApp-v1.4.0.apk](https://github.com/saferill/Nur-App/releases/download/v1.4.0/NurApp-v1.4.0.apk)

## Documentation

- Build notes: [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)
- Roadmap: [ROADMAP.md](ROADMAP.md)
- Contributing guide: [CONTRIBUTING.md](CONTRIBUTING.md)
- Content sources: [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md)

## Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | Fetch API content and check updates |
| `ACCESS_FINE_LOCATION` | GPS-based prayer location |
| `ACCESS_COARSE_LOCATION` | Fallback location access |
| `POST_NOTIFICATIONS` | Adhan and update notifications |
| `FOREGROUND_SERVICE` | Audio and adhan foreground service |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background playback |
| `SCHEDULE_EXACT_ALARM` | Exact prayer alarm scheduling |
| `USE_EXACT_ALARM` | Exact alarm support on compatible devices |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Better reminder reliability on aggressive OEM devices |
| `REQUEST_INSTALL_PACKAGES` | Install downloaded APK updates |
| `WRITE_EXTERNAL_STORAGE` | Legacy compatibility on older Android versions |
| `RECEIVE_BOOT_COMPLETED` | Repair schedules after reboot |
| `WAKE_LOCK` | Keep alarm flow stable during trigger |
| `USE_FULL_SCREEN_INTENT` | More visible urgent adhan flow when needed |

## Notes

- Signed release dijaga dengan keystore lama agar jalur update tidak putus.
- Beberapa identifier internal masih memakai nama lama untuk kompatibilitas.
- Localization sudah mulai dipisah ke resource locale, tapi masih terus dirapikan agar semakin lengkap.
- Fokus berikutnya ada di reliability, bukan sekadar menambah fitur baru.

## Contributing

Pull request dan perbaikan terarah dipersilakan. Sebelum kontribusi besar, baca dulu [CONTRIBUTING.md](CONTRIBUTING.md) dan cek [ROADMAP.md](ROADMAP.md) agar arah perubahan tetap sejalan dengan prioritas produk.

## License

Project ini dirilis di bawah [MIT License](LICENSE).

## Disclaimer

NurApp menggunakan beberapa public API dan sumber data pihak ketiga. Ketersediaan, format response, dan kebijakan source tersebut dapat berubah sewaktu-waktu sesuai provider masing-masing.
