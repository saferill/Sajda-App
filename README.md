<p align="center">
  <img src="docs/nurapp-logo.svg" alt="NurApp logo" width="180" />
</p>

<h1 align="center">NurApp</h1>

<p align="center">
  A modern Islamic Android app for Qur'an reading, adhan reminders, hadith search, Ramadan tools, and daily worship.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Native%20Android-0F5238?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Native Android" />
  <img src="https://img.shields.io/badge/Jetpack-Compose-2D6A4F?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Hilt-Retrofit%20%2B%20Room-D6B66A?style=for-the-badge&logo=google&logoColor=1A1A1A" alt="Hilt Retrofit Room" />
  <img src="https://img.shields.io/badge/Media3-Offline%20Audio-4B6359?style=for-the-badge&logo=android&logoColor=white" alt="Media3 Offline Audio" />
</p>

<p align="center">
  <a href="https://github.com/saferill/Sajda-App/releases/tag/v1.2.0">
    <img src="https://img.shields.io/badge/Latest%20Release-v1.2.0-0F5238?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v1.2.0" />
  </a>
  <a href="https://github.com/saferill/Sajda-App/releases/download/v1.2.0/NurApp-v1.2.0.apk">
    <img src="https://img.shields.io/badge/Download%20APK-NurApp%20v1.2.0-D6B66A?style=for-the-badge&logo=android&logoColor=1A1A1A" alt="Download NurApp v1.2.0 APK" />
  </a>
</p>

## Overview

NurApp is the current product direction for this repository. The GitHub repo name and Android package still use the older `Sajda App` and `com.sajda.app` identifiers to preserve signing, package continuity, and existing release history.

The app is built as an offline-first Android experience. Core worship flows such as Qur'an reading, bookmarks, downloaded audio, and scheduled adhan alerts remain usable locally, while hadith, doa, tafsir, prayer data, and release checks can refresh from public APIs when the network is available.

## Current Experience

### Main Navigation

- Six bottom tabs: Beranda, Qur'an, Adzan, Hadist, Ramadhan, Pengaturan
- Home dashboard with quick access to calendar and qibla from the top-right area
- Overlay-based secondary flows instead of fragmented multi-activity navigation
- Floating mini-player for active Qur'an audio playback

### Home

- Daily worship summary and quick actions
- Calendar panel with Hijri or Gregorian toggle
- Qibla compass shortcut beside the calendar entry point
- Prayer highlights, recent reading, and shortcut cards

### Qur'an

- Local Qur'an database seeded from bundled assets
- Surah reading with Arabic text, translation, bookmarks, and progress tracking
- Multiple qari choices for streaming and download
- Per-surah audio actions for play, download, and local removal
- Tafsir access with full tafsir content, not shortened summaries
- Search and reader overlay flows integrated into the main Qur'an experience

### Adzan

- Prayer times using saved or detected location
- Stronger exact-alarm scheduling and reboot rescheduling
- Foreground adhan playback service with duplicate-playback guard
- Fallback alert if the adhan service fails to start
- Emergency next-day fallback scheduling if normal repair fails
- Separate adhan sound selection for regular prayers and Subuh

### Hadith and Spiritual Content

- Hadith search screen with kitab selection
- Hadith data sourced from public API integrations
- Daily doa content and remote/offline fallback strategy
- Tafsir and supporting Islamic content sourced from integrated APIs

### Ramadan and Utilities

- Ramadan screen and supporting Islamic calendar flow
- Update center with GitHub release checks
- Widget and playback utility flows

### Settings

- Language preferences for app UI
- Adhan voice settings for regular prayers and Subuh
- Audio and qari preferences
- Appearance, location, and playback-related settings

## Data Sources

The app currently integrates or is prepared for these public sources:

- `AlQuran Cloud`
- `EQuran.id`
- `MyQuran`
- `Hadith Gading`
- `Quran.com` tafsir endpoints

Additional source notes are documented in [docs/CONTENT_SOURCES.md](docs/CONTENT_SOURCES.md).

## Tech Stack

| Layer | Stack |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository Pattern |
| Dependency Injection | Hilt |
| Local Data | Room |
| Preferences | DataStore |
| Networking | Retrofit + Gson + OkHttp |
| Audio | Media3 / ExoPlayer |
| Background Work | Foreground Service, AlarmManager, WorkManager |

## Build Requirements

- Android Studio with Android SDK installed
- JDK 17 or JDK 21
- Android SDK platform available for `compileSdk = 36`
- Existing signing config in `keystore.properties` for release builds

## Local Configuration

The project reads optional API-related values from `local.properties`:

```properties
hadith.api.key=
hadith.api.baseUrl=https://hadithapi.com/public/api
dua.content.url=https://raw.githubusercontent.com/wafaaelmaandy/Hisn-Muslim-Json/master/hisnmuslim.json
```

If you do not override them, the app uses the defaults defined in [app/build.gradle.kts](app/build.gradle.kts).

## Build

### Debug

```bash
./gradlew assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release

Release builds require the signing setup in `keystore.properties`.

```bash
./gradlew assembleRelease
```

Release APK:

```text
app/build/outputs/apk/release/NurApp-v1.2.0.apk
```

## Latest Release

- Release page: [v1.2.0](https://github.com/saferill/Sajda-App/releases/tag/v1.2.0)
- Direct APK: [NurApp-v1.2.0.apk](https://github.com/saferill/Sajda-App/releases/download/v1.2.0/NurApp-v1.2.0.apk)

## Project Structure

```text
app/src/main/
|- java/com/sajda/app/
|  |- data/
|  |  |- api/
|  |  |- local/
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
|  |- widget/
|  |- MainActivity.kt
|  `- SajdaApplication.kt
|- assets/
|- res/
`- AndroidManifest.xml
```

## Permissions

| Permission | Purpose |
|---|---|
| `ACCESS_FINE_LOCATION` | Automatic location for prayer times and qibla |
| `ACCESS_COARSE_LOCATION` | Fallback location access |
| `POST_NOTIFICATIONS` | Adhan and app notifications |
| `FOREGROUND_SERVICE` | Adhan and audio services |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background media playback |
| `SCHEDULE_EXACT_ALARM` | Exact prayer alarms |
| `USE_EXACT_ALARM` | Better exact alarm compatibility on supported devices |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Guidance for stronger reminder reliability |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after reboot |

## Contributing

Contribution guidelines are available in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This repository does not currently include a final license file.
