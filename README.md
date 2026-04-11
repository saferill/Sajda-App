<p align="center">
  <img src="docs/nurapp-logo.svg" alt="NurApp logo" width="180" />
</p>

<h1 align="center">NurApp</h1>

<p align="center">
  Aplikasi Android Muslim harian untuk Quran, adzan, hadist, Ramadhan, kiblat, kalender Islam, audio murattal, dan update APK dari GitHub Releases.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-0F5238?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-16423C?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Jetpack Compose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%7C%20Repository-D6B66A?style=for-the-badge&logo=android&logoColor=1A1A1A" alt="Architecture" />
  <img src="https://img.shields.io/badge/Release-v1.4.2-0F5238?style=for-the-badge&logo=github&logoColor=white" alt="Release v1.4.2" />
</p>

<p align="center">
  <a href="https://github.com/saferill/Nur-App/releases/tag/v1.4.2">Release Notes</a>
  |
  <a href="https://github.com/saferill/Nur-App/releases/download/v1.4.2/NurApp-v1.4.2.apk">Download APK</a>
  |
  <a href="ROADMAP.md">Roadmap</a>
  |
  <a href="LICENSE">License</a>
</p>

## Ringkasan

NurApp adalah aplikasi Android Kotlin yang dirancang untuk kebutuhan ibadah harian dengan tampilan yang lebih rapi, alur yang sederhana, dan fitur inti yang tetap berguna saat offline.

Brand aplikasi untuk pengguna adalah `NurApp`, sedangkan package Android tetap `com.sajda.app` agar kompatibilitas instalasi lama, signature, dan jalur update tidak putus.

## Fokus Produk

NurApp dibangun dengan arah yang jelas:

- cepat dibuka dan tidak terasa berat
- jadwal adzan dan alarm sholat lebih bisa diandalkan
- pengalaman membaca Quran lebih nyaman
- fitur tidak berantakan dan tidak penuh tombol ganda
- update aplikasi bisa dilakukan langsung dari dalam app

## Sorotan Fitur

### 1. Navigasi utama

- Beranda
- Quran
- Adzan
- Hadist
- Ramadhan
- Pengaturan

### 2. Quran

- Data Quran offline dari asset lokal
- Tampilan surah dan ayat dengan teks Arab, terjemahan, transliterasi, bookmark, dan last read
- Tafsir layar penuh
- Pilihan qari yang lebih banyak
- Download audio per surah
- Pilihan unduh qari aktif atau semua qari
- Opsi unduh dengan pembatasan Wi-Fi
- Mini player dan playback background

### 3. Adzan dan jadwal sholat

- Jadwal sholat harian
- Exact alarm scheduling
- Adzan reguler dan adzan Subuh terpisah
- Reschedule alarm setelah reboot
- Auto update lokasi adzan dari GPS
- Permission lokasi saat pertama kali aplikasi dibuka
- Halaman diagnosa adzan
- Countdown ke waktu sholat berikutnya

### 4. Hadist dan doa

- Pencarian hadist berdasarkan kitab, kata kunci, dan nomor
- Tipografi Arab yang lebih besar dan lebih nyaman
- Konten doa dan spiritual tambahan
- Highlight hasil pencarian

### 5. Ramadhan, kalender, dan kiblat

- Mode Ramadhan
- Jadwal imsak dan iftar
- Doa berbuka dan sahur
- Kalender Hijriah dan Masehi
- Akses kiblat dari alur utama aplikasi

### 6. Pengaturan dan update

- Bahasa aplikasi yang lebih luas
- Kontrol qari, mode baca, dan audio offline
- Update aplikasi manual lewat halaman `Update Aplikasi`
- Download APK update dari GitHub Releases
- Install APK update langsung dari aplikasi
- Backup dan restore data lokal inti

## Bahasa yang Didukung

Saat ini NurApp menyediakan pilihan bahasa berikut:

- Indonesia
- English
- Arabic
- Spanish
- German
- Portuguese
- Chinese
- Japanese
- Korean
- Italian
- Polish
- Ukrainian
- Swahili
- Tagalog
- Turkish
- Urdu
- French
- Malay
- Hindi

Catatan: untuk bahasa selain Indonesia dan English, aplikasi memakai jalur terjemahan cache agar transisi bahasa lebih luas bisa berjalan tanpa menggandakan semua string secara manual lebih dulu.

## Rilis Saat Ini

Versi terbaru yang sudah dipublikasikan adalah `v1.4.2`.

Perubahan penting di rilis ini:

- branding README dan release diselaraskan dengan logo aplikasi terbaru
- bahasa aplikasi diperluas dan tidak lagi berhenti di fallback English saja
- prompt lokasi awal untuk setup adzan ditambahkan
- opsi unduhan audio qari dibuat lebih jelas
- update center tetap manual, bukan auto-check saat app dibuka

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
| Update Delivery | GitHub Releases API + DownloadManager + Package Installer |

## Sumber Data

NurApp memakai kombinasi asset lokal dan public API:

- AlQuran Cloud
- EQuran.id
- MyQuran
- Hadith Gading
- Quran.com tafsir endpoints
- GitHub Releases API

Referensi source tambahan ada di [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md).

## Struktur Project

```text
app/src/main/
|- java/com/sajda/app/
|  |- data/
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

## Kebutuhan Build

- Android Studio yang mendukung AGP project ini
- Android SDK dengan `compileSdk = 36`
- JDK 17 atau JDK 21
- `keystore.properties` lama untuk signed release
- `local.properties` bila ingin override endpoint tertentu

## Konfigurasi Lokal

Project membaca nilai opsional berikut dari `local.properties`:

```properties
hadith.api.key=
hadith.api.baseUrl=https://hadithapi.com/public/api
dua.content.url=https://raw.githubusercontent.com/wafaaelmaandy/Hisn-Muslim-Json/master/hisnmuslim.json
```

Kalau tidak diisi, project memakai fallback dari `app/build.gradle.kts`.

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
app/build/outputs/apk/release/NurApp-v1.4.2.apk
```

## Install ke Device

Install lewat ADB:

```bash
adb install app/build/outputs/apk/release/NurApp-v1.4.2.apk
```

Kalau muncul error `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, berarti di device masih ada `com.sajda.app` dengan signature berbeda. Solusinya:

1. uninstall aplikasi lama dari device
2. install ulang APK release yang baru

## Update Aplikasi

Flow update saat ini bersifat manual:

1. buka `Pengaturan`
2. masuk ke `Update Aplikasi`
3. tekan `Cek sekarang`
4. unduh APK terbaru
5. tekan `Pasang sekarang`

NurApp tidak lagi memaksa cek update otomatis saat aplikasi dibuka.

## Dokumentasi

- Build notes: [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)
- Roadmap: [ROADMAP.md](ROADMAP.md)
- Contributing guide: [CONTRIBUTING.md](CONTRIBUTING.md)
- Content sources: [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md)

## Permission Utama

| Permission | Kegunaan |
|---|---|
| `INTERNET` | Ambil data API dan cek update |
| `ACCESS_FINE_LOCATION` | Lokasi GPS untuk adzan |
| `ACCESS_COARSE_LOCATION` | Fallback lokasi |
| `POST_NOTIFICATIONS` | Notifikasi adzan dan update |
| `FOREGROUND_SERVICE` | Audio dan service adzan |
| `SCHEDULE_EXACT_ALARM` | Alarm sholat presisi |
| `REQUEST_INSTALL_PACKAGES` | Install APK update |
| `RECEIVE_BOOT_COMPLETED` | Jadwal dipulihkan setelah reboot |
| `WAKE_LOCK` | Menjaga flow alarm saat trigger |

## Catatan Penting

- Signed release dijaga dengan keystore lama agar jalur update tidak putus.
- Beberapa identifier internal masih memakai nama lama demi kompatibilitas.
- Aplikasi masih terus dirapikan, terutama untuk reliability adzan, bahasa, dan penyederhanaan UI.

## Kontribusi

Pull request dan perbaikan terarah dipersilakan. Sebelum perubahan besar, baca [CONTRIBUTING.md](CONTRIBUTING.md) dan cek [ROADMAP.md](ROADMAP.md) agar arah kontribusi tetap sejalan.

## License

Project ini dirilis di bawah [MIT License](LICENSE).

## Disclaimer

NurApp memakai beberapa public API dan sumber data pihak ketiga. Ketersediaan endpoint, format response, dan kebijakan provider dapat berubah sewaktu-waktu.
