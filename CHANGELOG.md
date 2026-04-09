# Changelog

All notable changes to this project are documented in this file.

## Unreleased

### Planned next

- Continue improving adhan reliability across more Android devices
- Expand dua and hadith content with a richer offline-first dataset
- Refine prayer, qibla, and location UX
- Polish release workflow and public project documentation

## v1.4.0 - 2026-04-09

### Added

- Adhan diagnostics with more actionable readiness checks and last-event visibility
- Audio download controls for active qari or all qari, plus Wi-Fi-only handling
- Local backup and restore foundation for bookmarks and key settings
- Stronger Qur'an and hadith search helpers with result highlighting
- New roadmap document for upcoming product work
- New NurApp logo system for launcher, splash, and runtime branding

### Changed

- Refined Home, Qur'an, Hadith, Ramadan, and Settings flows to reduce duplicate actions and UI clutter
- Updated adhan playback so hardware volume follows the user's alarm stream more consistently
- Improved language handling groundwork with dedicated locale resources
- Hardened app update flow toward the current `saferill/Nur-App` release source

### Fixed

- Several mixed-language and placeholder UI issues across main screens
- Qur'an display issues related to broken separator characters and weaker empty states
- Notification and branding inconsistencies across launcher, splash, and alerts

## v1.1.2 - 2026-04-03

### Added

- Online hadith and dua content foundation with safe local API key loading
- Full English Qur'an translation asset and dynamic reading modes
- Adhan event history for diagnostics and recent troubleshooting
- More complete language handling across core screens and settings

### Changed

- Refined adhan notification layout to be more compact and avoid cropped heads-up content
- Improved adhan audio routing so hardware volume buttons follow the alarm stream while adhan is active
- Pulled saved location into adhan notifications when alarm payloads are missing location details
- Continued polishing the v1.2 foundation from `TODO.md`

### Fixed

- Adhan flow that could feel unreliable outside one prayer time on some devices
- Mixed Indonesian and English labels across key app flows
- Notification location fallback that previously showed an empty or not-selected state too often

## v1.1.1 - 2026-04-03

### Added

- Rich custom adhan notification layout with `Prayer`, `Snooze`, and `Stop` actions
- Hadith of the Day section inside the spiritual content flow
- Build scripts for signed release APK generation
- Content source notes for future hadith and dua expansion

### Changed

- Strengthened adhan scheduling with backup alarms and auto-repair behavior
- Improved release packaging and APK naming
- Refined splash, onboarding, and supporting UI flows

### Fixed

- Adhan notification flow that previously felt inconsistent outside Dzuhur
- Better foreground handling for adhan playback and reminder recovery
- Release build reliability on the current Windows environment
