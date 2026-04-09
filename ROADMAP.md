# NurApp Roadmap

Last updated: 2026-04-09

## Product Direction

NurApp is being shaped as a daily Muslim companion focused on reliability first:

- Qur'an reading, translation, tafsir, and murattal
- Prayer times and adhan reminders that users can trust
- Hadith and daily spiritual content
- Ramadan utilities, Islamic calendar, and qibla
- Offline-friendly experience with a clean and calm interface

The main rule for the next phase is simple:

**Do not add more major features before the core flows are stable.**

## Current Priorities

The app already has broad feature coverage, but the next stage should focus on product quality:

1. Adhan reliability
2. Full language support
3. UI cleanup and navigation structure
4. Data consistency and backup
5. Automated tests for critical flows

## 30-Day Plan

### Phase 1: Reliability First

Target: make adhan trustworthy on real devices.

- Audit `AdzanService`, `AdzanReceiver`, `AdzanScheduler`, and `BootReceiver`
- Verify exact alarm handling across Android versions
- Improve reboot recovery and daily reschedule flow
- Improve battery optimization guidance and diagnostics
- Make adhan volume fully follow the user's alarm volume
- Add a proper manual `Test Adhan` flow
- Persist and display last successful adhan trigger
- Add clearer logs for alarm scheduling, trigger, playback start, playback finish, and failure

Definition of done:

- Adhan triggers on time on real devices
- Adhan still works after reboot
- Diagnostics show useful status, not placeholder info
- Failure cases still produce a visible fallback alert

### Phase 2: Language and Copy Cleanup

Target: make app language switching feel real, not partial.

- Move remaining hardcoded UI text into string resources
- Finish Indonesian and English as the first complete language pair
- Use other locales only when the strings are actually available
- Remove mixed Indonesian and English text in the same screen
- Standardize prayer labels, action labels, and settings copy
- Remove tutorial-like helper text that makes screens feel unfinished

Definition of done:

- Switching app language updates the full main shell
- No visible mixed-language screens in normal flows
- No broken characters or bad encoding in Qur'an and hadith screens

## 60-Day Plan

### Phase 3: UX Simplification

Target: make the app feel calmer and easier to use.

- Reduce duplicate entry points and duplicate feature buttons
- Keep bottom navigation focused on high-value destinations
- Simplify Home so it highlights only the most-used actions
- Simplify Settings into clear sections:
  - Adhan
  - Qur'an audio
  - Language and appearance
  - Location and prayer schedule
  - Data and updates
- Remove placeholder overlays and dead routes
- Reduce visual clutter from overly descriptive helper text
- Improve empty states and error states

Definition of done:

- Home is readable at a glance
- Settings no longer feel crowded
- Each feature has one clear entry path

### Phase 4: Audio and Download Experience

Target: make murattal download and playback easier to understand.

- Support `active reciter only` and `all reciters` download modes
- Support `Wi-Fi only` audio downloads
- Show estimated file size before download
- Add `delete all audio` flow
- Verify playback fallback when a selected reciter file is missing
- Improve download progress and failure messaging

Definition of done:

- Users understand what will be downloaded
- Users can manage storage intentionally
- Audio playback remains predictable after reciter changes

## 90-Day Plan

### Phase 5: Data Safety and Maintainability

Target: reduce long-term bug risk.

- Audit overlap between Room, DataStore, and SharedPreferences
- Reduce duplicated state where possible
- Harden backup and restore for:
  - bookmarks
  - last read
  - adhan settings
  - selected qari
  - selected language
- Harden GitHub updater with retry, checksum validation, and better asset handling
- Remove old repo references and stale update links

Definition of done:

- Restore results are predictable
- Settings do not silently drift between storage layers
- Update flow fails safely and clearly

### Phase 6: Test Coverage for Core Flows

Target: protect the app from regressions.

- Add unit tests for:
  - adhan scheduling logic
  - location update decision logic
  - update release parsing and version comparison
  - audio download planning
- Add targeted integration or instrumentation tests for:
  - adhan scheduling after boot
  - language switching
  - update banner states
  - backup and restore

Definition of done:

- Critical logic changes are covered before release
- Regressions are caught before they reach users

## What Should Be Added

- Adhan diagnostics page with actionable status
- Stronger Qur'an and hadith search with highlighting
- Better backup and restore
- Safer update download and install flow
- Better empty states and error states
- More polished release checklist before every production build

## What Should Be Removed or Reduced

- Dead callbacks and dead routes
- Duplicate feature buttons
- Hardcoded fallback locations in user-facing screens
- Mixed-language copy
- Placeholder overlays
- Excessive helper text that does not feel like a finished product
- Broken or legacy branding references

## What Not To Do Yet

Do not prioritize these before the core reliability work is done:

- adding more tabs
- adding social features
- adding login or cloud sync
- redesigning every screen from zero
- adding more APIs without cleaning the existing flows

## Release Strategy

### v1.3.x

Focus on fixes and reliability:

- adhan stability
- volume behavior
- icon and splash consistency
- settings cleanup
- bug fixes from real-device feedback

### v1.4.0

Focus on product trust:

- diagnostics page complete
- localization cleanup complete for core languages
- backup and restore stable
- stronger updater

### v1.5.0

Focus on maintainability:

- reduced state duplication
- cleaner architecture
- stronger automated test coverage

## Working Rules

Before shipping a release:

1. Test adhan on a real device
2. Test reboot behavior
3. Test language switching
4. Test audio download and playback
5. Test updater flow
6. Review GitHub release assets and version tags

## Success Criteria

NurApp is on the right track if users can say these things:

- "Adhan always works."
- "The app feels calm and clear."
- "Language switching actually changes the app."
- "Downloading and playing Qur'an audio is easy."
- "Updates are simple and safe."

