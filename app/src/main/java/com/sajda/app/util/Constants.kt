package com.sajda.app.util

import com.sajda.app.domain.model.CityPreset

object Constants {
    const val APP_NAME = "Sajda App"
    const val DATABASE_NAME = "sajda_database"
    const val QURAN_TRANSLATION_ASSET = "quran_id.json"
    const val QURAN_TRANSLITERATION_ASSET = "quran_transliteration.json"

    const val AUDIO_DOWNLOAD_DIR = "murattal"
    const val AUDIO_NOTIFICATION_CHANNEL = "audio_playback_channel"
    const val ADZAN_NOTIFICATION_CHANNEL = "adzan_channel"
    const val AUDIO_NOTIFICATION_ID = 1201
    const val ADZAN_NOTIFICATION_ID = 1202

    const val ACTION_PLAY_AUDIO = "com.sajda.app.action.PLAY_AUDIO"
    const val ACTION_PAUSE_AUDIO = "com.sajda.app.action.PAUSE_AUDIO"
    const val ACTION_RESUME_AUDIO = "com.sajda.app.action.RESUME_AUDIO"
    const val ACTION_STOP_AUDIO = "com.sajda.app.action.STOP_AUDIO"
    const val ACTION_TRIGGER_ADZAN = "com.sajda.app.action.TRIGGER_ADZAN"
    const val ACTION_STOP_ADZAN = "com.sajda.app.action.STOP_ADZAN"
    const val ACTION_SNOOZE_ADZAN = "com.sajda.app.action.SNOOZE_ADZAN"
    const val ACTION_OPEN_PRAYER_TAB = "com.sajda.app.action.OPEN_PRAYER_TAB"
    const val ACTION_REFRESH_PRAYER_WIDGET = "com.sajda.app.action.REFRESH_PRAYER_WIDGET"

    const val EXTRA_SURAH_NUMBER = "surah_number"
    const val EXTRA_SURAH_TITLE = "surah_title"
    const val EXTRA_AUDIO_PATH = "audio_path"
    const val EXTRA_PRAYER_NAME = "prayer_name"
    const val EXTRA_OPEN_TAB = "open_tab"
    const val EXTRA_SNOOZE_MINUTES = "snooze_minutes"

    const val PRAYER_SCHEDULE_WORK_NAME = "prayer_schedule_refresh"

    fun buildMurattalUrl(surahNumber: Int): String {
        val paddedNumber = surahNumber.toString().padStart(3, '0')
        return "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$paddedNumber.mp3"
    }

    fun formatAudioFileName(surahNumber: Int): String = "surah_${surahNumber.toString().padStart(3, '0')}.mp3"
}

object LocationConstants {
    const val DEFAULT_LATITUDE = -6.2088
    const val DEFAULT_LONGITUDE = 106.8456
    const val DEFAULT_LOCATION = "Jakarta"

    val cityPresets = listOf(
        CityPreset("Jakarta", -6.2088, 106.8456),
        CityPreset("Bandung", -6.9175, 107.6191),
        CityPreset("Semarang", -6.9667, 110.4167),
        CityPreset("Yogyakarta", -7.7956, 110.3695),
        CityPreset("Surabaya", -7.2575, 112.7521),
        CityPreset("Malang", -7.9666, 112.6326),
        CityPreset("Solo", -7.5666, 110.8167),
        CityPreset("Bogor", -6.5950, 106.8166),
        CityPreset("Depok", -6.4025, 106.7942),
        CityPreset("Tangerang", -6.1783, 106.6319),
        CityPreset("Bekasi", -6.2383, 106.9756),
        CityPreset("Cirebon", -6.7320, 108.5523),
        CityPreset("Tasikmalaya", -7.3506, 108.2172),
        CityPreset("Serang", -6.1201, 106.1503),
        CityPreset("Cilegon", -6.0025, 106.0112),
        CityPreset("Denpasar", -8.6705, 115.2126),
        CityPreset("Mataram", -8.5833, 116.1167),
        CityPreset("Palembang", -2.9761, 104.7754),
        CityPreset("Pekanbaru", 0.5071, 101.4478),
        CityPreset("Padang", -0.9471, 100.4172),
        CityPreset("Medan", 3.5952, 98.6722),
        CityPreset("Banda Aceh", 5.5483, 95.3238),
        CityPreset("Batam", 1.0456, 104.0305),
        CityPreset("Pontianak", -0.0263, 109.3425),
        CityPreset("Banjarmasin", -3.3186, 114.5944),
        CityPreset("Balikpapan", -1.2379, 116.8529),
        CityPreset("Samarinda", -0.5022, 117.1537),
        CityPreset("Manado", 1.4748, 124.8421),
        CityPreset("Palu", -0.8917, 119.8707),
        CityPreset("Makassar", -5.1477, 119.4327),
        CityPreset("Kendari", -3.9985, 122.5127),
        CityPreset("Ambon", -3.6547, 128.1903),
        CityPreset("Ternate", 0.7893, 127.3842),
        CityPreset("Jayapura", -2.5337, 140.7181)
    )
}
