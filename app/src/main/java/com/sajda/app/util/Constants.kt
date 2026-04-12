package com.sajda.app.util

import com.sajda.app.domain.model.CityPreset

object Constants {
    const val APP_NAME = "NurApp"
    const val DATABASE_NAME = "sajda_database"
    const val QURAN_TRANSLATION_ASSET = "quran_id.json"
    const val QURAN_ENGLISH_ASSET = "quran_en.json"
    const val QURAN_TRANSLITERATION_ASSET = "quran_transliteration.json"

    const val AUDIO_DOWNLOAD_DIR = "murattal"
    const val AUDIO_NOTIFICATION_CHANNEL = "audio_playback_channel"
    const val ADZAN_NOTIFICATION_CHANNEL = "adzan_channel"
    const val ADZAN_ALERT_NOTIFICATION_CHANNEL = "adzan_alert_channel"
    const val ADZAN_RECOVERY_NOTIFICATION_CHANNEL = "adzan_recovery_channel"
    const val UPDATE_NOTIFICATION_CHANNEL = "app_update_channel"
    const val AUDIO_NOTIFICATION_ID = 1201
    const val ADZAN_NOTIFICATION_ID = 1202
    const val UPDATE_AVAILABLE_NOTIFICATION_ID = 1203
    const val UPDATE_READY_NOTIFICATION_ID = 1204
    const val ADZAN_RECOVERY_NOTIFICATION_ID = 1205

    const val ACTION_PLAY_AUDIO = "com.sajda.app.action.PLAY_AUDIO"
    const val ACTION_PAUSE_AUDIO = "com.sajda.app.action.PAUSE_AUDIO"
    const val ACTION_RESUME_AUDIO = "com.sajda.app.action.RESUME_AUDIO"
    const val ACTION_STOP_AUDIO = "com.sajda.app.action.STOP_AUDIO"
    const val ACTION_TRIGGER_ADZAN = "com.sajda.app.action.TRIGGER_ADZAN"
    const val ACTION_STOP_ADZAN = "com.sajda.app.action.STOP_ADZAN"
    const val ACTION_SNOOZE_ADZAN = "com.sajda.app.action.SNOOZE_ADZAN"
    const val ACTION_OPEN_PRAYER_TAB = "com.sajda.app.action.OPEN_PRAYER_TAB"
    const val ACTION_REFRESH_PRAYER_WIDGET = "com.sajda.app.action.REFRESH_PRAYER_WIDGET"
    const val ACTION_START_APP_UPDATE_DOWNLOAD = "com.sajda.app.action.START_APP_UPDATE_DOWNLOAD"

    const val EXTRA_SURAH_NUMBER = "surah_number"
    const val EXTRA_SURAH_TITLE = "surah_title"
    const val EXTRA_AUDIO_PATH = "audio_path"
    const val EXTRA_PRAYER_NAME = "prayer_name"
    const val EXTRA_PRAYER_KEY = "prayer_key"
    const val EXTRA_PRAYER_TIME = "prayer_time"
    const val EXTRA_PRAYER_DATE = "prayer_date"
    const val EXTRA_LOCATION_NAME = "location_name"
    const val EXTRA_OPEN_TAB = "open_tab"
    const val EXTRA_SNOOZE_MINUTES = "snooze_minutes"
    const val EXTRA_UPDATE_VERSION_NAME = "update_version_name"
    const val EXTRA_UPDATE_RELEASE_NAME = "update_release_name"
    const val EXTRA_UPDATE_NOTES = "update_notes"
    const val EXTRA_UPDATE_DOWNLOAD_URL = "update_download_url"
    const val EXTRA_UPDATE_RELEASE_PAGE_URL = "update_release_page_url"
    const val EXTRA_UPDATE_PUBLISHED_AT = "update_published_at"

    const val PRAYER_SCHEDULE_WORK_NAME = "prayer_schedule_refresh"
    const val APP_UPDATE_WORK_NAME = "app_update_check"
    const val UPDATE_DOWNLOAD_TITLE = "NurApp update"
    const val UPDATE_RELEASES_URL = "https://api.github.com/repos/saferill/Nur-App/releases/latest"
    const val UPDATE_RELEASES_PAGE_URL = "https://github.com/saferill/Nur-App/releases"

    fun buildMurattalUrl(surahNumber: Int): String {
        val paddedNumber = surahNumber.toString().padStart(3, '0')
        return "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$paddedNumber.mp3"
    }

    fun formatAudioFileName(surahNumber: Int, reciterId: String = "05"): String {
        val paddedNumber = surahNumber.toString().padStart(3, '0')
        return "surah_${paddedNumber}_$reciterId.mp3"
    }
}

object LocationConstants {
    const val DEFAULT_LATITUDE = -6.2088
    const val DEFAULT_LONGITUDE = 106.8456
    const val DEFAULT_LOCATION = "Jakarta"

    private fun province(
        name: String,
        latitude: Double,
        longitude: Double,
        vararg aliases: String
    ) = CityPreset(
        name = name,
        latitude = latitude,
        longitude = longitude,
        areaType = "Provinsi",
        aliases = aliases.toList()
    )

    private fun city(
        name: String,
        province: String,
        latitude: Double,
        longitude: Double,
        vararg aliases: String
    ) = CityPreset(
        name = name,
        latitude = latitude,
        longitude = longitude,
        areaType = "Kota",
        province = province,
        aliases = aliases.toList()
    )

    private fun regency(
        name: String,
        province: String,
        latitude: Double,
        longitude: Double,
        vararg aliases: String
    ) = CityPreset(
        name = name,
        latitude = latitude,
        longitude = longitude,
        areaType = "Kabupaten",
        province = province,
        aliases = aliases.toList()
    )

    val cityPresets = listOf(
        province("Aceh", 5.5483, 95.3238, "Banda Aceh"),
        province("Sumatera Utara", 3.5952, 98.6722, "Medan"),
        province("Sumatera Barat", -0.9471, 100.4172, "Padang"),
        province("Riau", 0.5071, 101.4478, "Pekanbaru"),
        province("Kepulauan Riau", 0.9186, 104.4665, "Tanjung Pinang", "Batam"),
        province("Jambi", -1.6101, 103.6131),
        province("Bengkulu", -3.7928, 102.2608),
        province("Sumatera Selatan", -2.9761, 104.7754, "Palembang"),
        province("Kepulauan Bangka Belitung", -2.1291, 106.1138, "Pangkal Pinang"),
        province("Lampung", -5.4292, 105.2610, "Bandar Lampung"),
        province("Banten", -6.1201, 106.1503, "Serang", "Tangerang", "Cilegon"),
        province("DKI Jakarta", -6.1754, 106.8272, "Jakarta"),
        province("Jawa Barat", -6.9175, 107.6191, "Bandung", "Bogor", "Bekasi"),
        province("Jawa Tengah", -6.9667, 110.4167, "Semarang", "Solo"),
        province("DI Yogyakarta", -7.7956, 110.3695, "Yogyakarta"),
        province("Jawa Timur", -7.2575, 112.7521, "Surabaya", "Malang"),
        province("Bali", -8.6705, 115.2126, "Denpasar"),
        province("Nusa Tenggara Barat", -8.5833, 116.1167, "Mataram"),
        province("Nusa Tenggara Timur", -10.1772, 123.6070, "Kupang"),
        province("Kalimantan Barat", -0.0263, 109.3425, "Pontianak"),
        province("Kalimantan Tengah", -2.2088, 113.9167, "Palangka Raya"),
        province("Kalimantan Selatan", -3.3186, 114.5944, "Banjarmasin", "Banjarbaru"),
        province("Kalimantan Timur", -0.5022, 117.1537, "Samarinda", "Balikpapan"),
        province("Kalimantan Utara", 3.3274, 117.5785, "Tanjung Selor", "Tarakan"),
        province("Sulawesi Utara", 1.4748, 124.8421, "Manado"),
        province("Gorontalo", 0.5412, 123.0595),
        province("Sulawesi Tengah", -0.8917, 119.8707, "Palu"),
        province("Sulawesi Barat", -2.6806, 118.8867, "Mamuju"),
        province("Sulawesi Selatan", -5.1477, 119.4327, "Makassar"),
        province("Sulawesi Tenggara", -3.9985, 122.5127, "Kendari"),
        province("Maluku", -3.6547, 128.1903, "Ambon"),
        province("Maluku Utara", 0.7893, 127.3842, "Ternate", "Sofifi"),
        province("Papua", -2.5337, 140.7181, "Jayapura"),
        province("Papua Barat", -0.8615, 134.0620, "Manokwari"),
        province("Papua Barat Daya", -0.8762, 131.2558, "Sorong"),
        province("Papua Selatan", -8.4932, 140.4018, "Merauke"),
        province("Papua Tengah", -3.3667, 135.5000, "Nabire"),
        province("Papua Pegunungan", -4.0967, 138.9517, "Wamena"),

        city("Banda Aceh", "Aceh", 5.5483, 95.3238),
        regency("Aceh Besar", "Aceh", 5.4522, 95.3256),
        city("Medan", "Sumatera Utara", 3.5952, 98.6722),
        regency("Deli Serdang", "Sumatera Utara", 3.5620, 98.8841),
        city("Padang", "Sumatera Barat", -0.9471, 100.4172),
        city("Bukittinggi", "Sumatera Barat", -0.3056, 100.3692),
        city("Pekanbaru", "Riau", 0.5071, 101.4478),
        regency("Kampar", "Riau", 0.3244, 101.1500),
        city("Batam", "Kepulauan Riau", 1.0456, 104.0305),
        city("Tanjung Pinang", "Kepulauan Riau", 0.9186, 104.4665),
        city("Jambi", "Jambi", -1.6101, 103.6131),
        city("Bengkulu", "Bengkulu", -3.7928, 102.2608),
        city("Palembang", "Sumatera Selatan", -2.9761, 104.7754),
        city("Bandar Lampung", "Lampung", -5.4292, 105.2610),
        city("Pangkal Pinang", "Kepulauan Bangka Belitung", -2.1291, 106.1138),
        city("Serang", "Banten", -6.1201, 106.1503),
        city("Cilegon", "Banten", -6.0025, 106.0112),
        city("Tangerang", "Banten", -6.1783, 106.6319),
        city("Tangerang Selatan", "Banten", -6.2886, 106.7179, "Tangsel"),
        city("Jakarta Pusat", "DKI Jakarta", -6.1754, 106.8272),
        city("Jakarta Selatan", "DKI Jakarta", -6.2615, 106.8106),
        city("Jakarta Timur", "DKI Jakarta", -6.2250, 106.9004),
        city("Jakarta Barat", "DKI Jakarta", -6.1683, 106.7588),
        city("Jakarta Utara", "DKI Jakarta", -6.1384, 106.8637),
        city("Bandung", "Jawa Barat", -6.9175, 107.6191),
        city("Cimahi", "Jawa Barat", -6.8722, 107.5425),
        city("Bogor", "Jawa Barat", -6.5950, 106.8166),
        regency("Bogor", "Jawa Barat", -6.5971, 106.8060),
        city("Bekasi", "Jawa Barat", -6.2383, 106.9756),
        regency("Bandung", "Jawa Barat", -6.9846, 107.6236),
        city("Cirebon", "Jawa Barat", -6.7320, 108.5523),
        city("Tasikmalaya", "Jawa Barat", -7.3506, 108.2172),
        city("Sukabumi", "Jawa Barat", -6.9240, 106.9274),
        regency("Garut", "Jawa Barat", -7.2279, 107.9087),
        city("Semarang", "Jawa Tengah", -6.9667, 110.4167),
        city("Surakarta", "Jawa Tengah", -7.5666, 110.8167, "Solo"),
        city("Magelang", "Jawa Tengah", -7.4797, 110.2177),
        regency("Kudus", "Jawa Tengah", -6.8048, 110.8405),
        regency("Banyumas", "Jawa Tengah", -7.4242, 109.2396, "Purwokerto"),
        city("Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695),
        regency("Sleman", "DI Yogyakarta", -7.7164, 110.3556),
        regency("Bantul", "DI Yogyakarta", -7.8886, 110.3289),
        city("Surabaya", "Jawa Timur", -7.2575, 112.7521),
        city("Malang", "Jawa Timur", -7.9666, 112.6326),
        regency("Sidoarjo", "Jawa Timur", -7.4467, 112.7183),
        regency("Gresik", "Jawa Timur", -7.1568, 112.6550),
        regency("Jember", "Jawa Timur", -8.1724, 113.7003),
        city("Kediri", "Jawa Timur", -7.8169, 112.0114),
        city("Denpasar", "Bali", -8.6705, 115.2126),
        regency("Badung", "Bali", -8.5810, 115.1770),
        city("Mataram", "Nusa Tenggara Barat", -8.5833, 116.1167),
        regency("Lombok Barat", "Nusa Tenggara Barat", -8.7064, 116.0886),
        city("Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070),
        city("Pontianak", "Kalimantan Barat", -0.0263, 109.3425),
        regency("Kubu Raya", "Kalimantan Barat", -0.0833, 109.3000),
        city("Palangka Raya", "Kalimantan Tengah", -2.2088, 113.9167),
        city("Banjarmasin", "Kalimantan Selatan", -3.3186, 114.5944),
        city("Banjarbaru", "Kalimantan Selatan", -3.4421, 114.8457),
        city("Balikpapan", "Kalimantan Timur", -1.2379, 116.8529),
        city("Samarinda", "Kalimantan Timur", -0.5022, 117.1537),
        city("Tarakan", "Kalimantan Utara", 3.3274, 117.5785),
        city("Manado", "Sulawesi Utara", 1.4748, 124.8421),
        city("Tomohon", "Sulawesi Utara", 1.3236, 124.8408),
        city("Gorontalo", "Gorontalo", 0.5412, 123.0595),
        city("Palu", "Sulawesi Tengah", -0.8917, 119.8707),
        city("Mamuju", "Sulawesi Barat", -2.6806, 118.8867),
        city("Makassar", "Sulawesi Selatan", -5.1477, 119.4327),
        regency("Gowa", "Sulawesi Selatan", -5.3166, 119.4667),
        city("Kendari", "Sulawesi Tenggara", -3.9985, 122.5127),
        city("Ambon", "Maluku", -3.6547, 128.1903),
        city("Ternate", "Maluku Utara", 0.7893, 127.3842),
        city("Jayapura", "Papua", -2.5337, 140.7181),
        city("Sorong", "Papua Barat Daya", -0.8762, 131.2558),
        city("Merauke", "Papua Selatan", -8.4932, 140.4018),
        regency("Mimika", "Papua Tengah", -4.5283, 136.8870, "Timika"),
        city("Nabire", "Papua Tengah", -3.3667, 135.5000),
        city("Wamena", "Papua Pegunungan", -4.0967, 138.9517),
        city("Manokwari", "Papua Barat", -0.8615, 134.0620)
    )
}
