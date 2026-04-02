package com.sajda.app.util

import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.DailyDua

object SpiritualContent {
    val dailyDuas = listOf(
        DailyDua(
            id = "morning_istighfar",
            category = "Morning",
            title = "Sayyidul Istighfar",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ",
            transliteration = "Allahumma anta rabbi la ilaha illa anta khalaqtani wa ana 'abduka.",
            translation = "Ya Allah, Engkaulah Tuhanku, tiada ilah selain Engkau. Engkau menciptakanku dan aku adalah hamba-Mu."
        ),
        DailyDua(
            id = "morning_protection",
            category = "Morning",
            title = "Dua Perlindungan Pagi",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ",
            transliteration = "Bismillahil ladzi la yadurru ma'asmihi syai'un.",
            translation = "Dengan nama Allah yang dengan nama-Nya tidak ada sesuatu pun yang dapat memberi mudarat."
        ),
        DailyDua(
            id = "evening_tawakkal",
            category = "Evening",
            title = "Dua Tawakal",
            arabic = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ",
            transliteration = "Hasbiyallahu la ilaha illa huwa 'alaihi tawakkaltu.",
            translation = "Cukuplah Allah bagiku, tiada ilah selain Dia. Hanya kepada-Nya aku bertawakal."
        ),
        DailyDua(
            id = "before_sleep",
            category = "Daily Activities",
            title = "Sebelum Tidur",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya.",
            translation = "Dengan nama-Mu ya Allah aku mati dan aku hidup."
        ),
        DailyDua(
            id = "leaving_home",
            category = "Daily Activities",
            title = "Keluar Rumah",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ",
            transliteration = "Bismillah, tawakkaltu 'alallah.",
            translation = "Dengan nama Allah, aku bertawakal kepada Allah."
        ),
        DailyDua(
            id = "after_adhan",
            category = "Prayer",
            title = "Setelah Adzan",
            arabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ",
            transliteration = "Allahumma rabba hadzihid da'watit tammah.",
            translation = "Ya Allah, Tuhan pemilik seruan yang sempurna ini."
        )
    )

    fun buildTafsir(ayat: Ayat, surahName: String): List<String> {
        val normalized = ayat.translation.trim()
        val emphasis = when {
            normalized.contains("rahmat", ignoreCase = true) -> "Ayat ini mengingatkan bahwa kasih sayang Allah selalu lebih luas daripada rasa takut kita."
            normalized.contains("sabar", ignoreCase = true) -> "Pesan utamanya adalah keteguhan: tetap lurus, tenang, dan tidak tergesa-gesa saat diuji."
            normalized.contains("shalat", ignoreCase = true) -> "Ayat ini menegaskan shalat sebagai poros ketenangan, disiplin, dan hubungan hamba dengan Rabb-nya."
            normalized.contains("iman", ignoreCase = true) -> "Ayat ini menguatkan fondasi iman: percaya, taat, lalu membiarkan amal berbicara."
            else -> "Ayat ini mengajak kita berhenti sejenak, membaca perlahan, lalu menanyakan apa yang harus dibenahi dalam hati hari ini."
        }

        return listOf(
            "$surahName ayat ${ayat.ayatNumber} menegaskan makna: $normalized",
            emphasis,
            "Amalan ringan hari ini: baca ulang ayat ini beberapa kali, lalu hubungkan maknanya dengan keadaan yang sedang Anda jalani."
        )
    }
}
