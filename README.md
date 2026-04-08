<p align="center">
  <img src="app/src/main/res/drawable-nodpi/sajda_logo_full.png" alt="NurApp" width="220" />
</p>

<h1 align="center">NurApp</h1>

<p align="center">
  A modern Islamic Android app for Qur'an reading, prayer times, adhan alerts, qibla, and daily spiritual content.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Native%20Android-0F5238?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Native Android" />
  <img src="https://img.shields.io/badge/Jetpack-Compose-2D6A4F?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Hilt-Room%20%2B%20DataStore-D6B66A?style=for-the-badge&logo=google&logoColor=1A1A1A" alt="Hilt Room DataStore" />
  <img src="https://img.shields.io/badge/Offline-First-4B6359?style=for-the-badge&logo=android&logoColor=white" alt="Offline First" />
</p>

<p align="center">
  <a href="https://github.com/saferill/Sajda-App/releases/tag/v1.1.3">
    <img src="https://img.shields.io/badge/Latest%20Release-v1.1.3-0F5238?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v1.1.3" />
  </a>
  <a href="https://github.com/saferill/Sajda-App/releases/download/v1.1.3/NurApp-v1.1.3.apk">
    <img src="https://img.shields.io/badge/Download%20APK-NurApp%20v1.1.3-D6B66A?style=for-the-badge&logo=android&logoColor=1A1A1A" alt="Download NurApp v1.1.3 APK" />
  </a>
</p>

## About

NurApp is the current product name of this project. The repository name and Android package still use the older `Sajda App` / `com.sajda.app` identifiers for continuity with the existing codebase, signing setup, and release history.

The app is built as an offline-first Android experience: core Qur'an reading, bookmarks, reading progress, downloaded audio, and scheduled prayer reminders remain available locally, while spiritual content and update checks can refresh from remote sources when the network is available.

## Current Feature Set

### Home and Navigation

- Five-tab shell: Home, Qur'an, Prayer, Qibla, More
- Onboarding flow for first-time setup
- Floating mini player for ongoing audio playback
- Overlay-based secondary screens instead of fragmented multi-activity navigation

### Qur'an Experience

- Local Qur'an database seeded from bundled assets
- Surah list and detailed reading screen
- Arabic text, translation, and reading controls
- Search across Qur'an content
- Last-read tracking and bookmarks
- Tafsir overlay flow from the current reader experience
- Per-surah audio download and playback support

### Prayer and Adhan

- Prayer times with automatic or saved location
- Weekly prayer schedule and qibla screen
- Per-prayer adhan preferences and manual adhan testing
- Exact alarm scheduling with boot reschedule support
- Foreground adhan playback service
- Device-focused reliability improvements:
  - wake lock during alarm handling
  - duplicate playback guard
  - fallback alert if the adhan service fails to start
  - emergency next-day fallback scheduling if repair fails
  - notification permission and battery-related guidance flows

### Spiritual Content

- Daily dua screen
- Hadith library screen
- Islamic calendar and Ramadan mode screens
- Mixed remote/offline content strategy
- Remote content sources currently integrated in the app:
  - `EQuran.id`
  - `MyQuran`
  - `Hadith Gading`

### Settings and Utilities

- Appearance, language, and location settings
- Adhan sound selection and playback controls
- Downloaded audio management
- Widget preview screen
- Update center screen with GitHub release checks

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
| Audio | Media3 / ExoPlayer + Android media APIs |
| Background Work | Foreground Service, AlarmManager, WorkManager |

## Build Requirements

- Android Studio with Android SDK installed
- JDK 17
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

## Getting Started

### Debug build

```bash
./gradlew assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release build

Release builds require the existing signing setup in `keystore.properties`.

```bash
./gradlew assembleRelease
```

Release APK output:

```text
app/build/outputs/apk/release/NurApp-v1.1.3.apk
```

## Latest Release

- Release page: [v1.1.3](https://github.com/saferill/Sajda-App/releases/tag/v1.1.3)
- Direct APK: [NurApp-v1.1.3.apk](https://github.com/saferill/Sajda-App/releases/download/v1.1.3/NurApp-v1.1.3.apk)
- Changelog: [CHANGELOG.md](CHANGELOG.md)

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

## Development Notes

- The codebase is actively evolving from the original Sajda App foundation into the current NurApp product direction.
- Core worship flows are intended to stay useful without forcing account creation or cloud sync.
- Remote content is used as an enhancement layer, not as a hard dependency for the entire app.

## Contributing

Contribution guidelines are available in [CONTRIBUTING.md](CONTRIBUTING.md).

Useful contribution areas:

- Adhan reliability across more devices
- Compose UI polish and cleanup
- Qur'an reader improvements
- Qibla, prayer, and location UX refinement
- Documentation and release process improvements

## License

This repository does not currently include a final license file.
