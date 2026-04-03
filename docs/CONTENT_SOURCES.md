# Spiritual Content Sources

This note tracks realistic data sources for expanding Sajda App's dua and hadith features.

## Hadith APIs

- Hadith API: https://hadithapi.com/
  - Requires an API key.
  - The docs state that the key is generated after registering and logging in.
  - Supports books, chapters, and hadith search endpoints.

- Sunnah.com official API repository: https://github.com/sunnah-com/api
  - Official source repository for the Sunnah.com API.
  - Useful as a long-term integration target or self-hosted reference.

## Dua / Hisn al-Muslim

- Hisn Muslim JSON dataset: https://github.com/wafaaelmaandy/Hisn-Muslim-Json
  - Lightweight JSON source that can be adapted for offline dua bundling.
  - Good candidate for a bundled offline-first dataset inside the app.

## Current App Approach

- Sajda App currently uses bundled offline spiritual content first.
- Hadith of the Day is now available locally in the app.
- For a future live hadith sync, prefer adding a user-owned API key instead of hardcoding one in the repository.
