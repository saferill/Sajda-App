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
            title = "Morning Protection",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ",
            transliteration = "Bismillahil ladhi la yadurru ma'asmihi shay'un fil ardi wa la fis-sama'.",
            translation = "Dengan nama Allah yang dengan nama-Nya tidak ada sesuatu pun di bumi dan langit yang dapat membahayakan."
        ),
        DailyDua(
            id = "morning_contentment",
            category = "Morning",
            title = "Ridha with Allah",
            arabic = "رَضِيتُ بِاللَّهِ رَبًّا وَبِالْإِسْلَامِ دِينًا وَبِمُحَمَّدٍ نَبِيًّا",
            transliteration = "Raditu billahi rabba, wa bil-islami dina, wa bi Muhammadin nabiyya.",
            translation = "Aku ridha Allah sebagai Rabb-ku, Islam sebagai agamaku, dan Muhammad sebagai nabiku."
        ),
        DailyDua(
            id = "morning_start",
            category = "Morning",
            title = "Start of Day",
            arabic = "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ",
            transliteration = "Allahumma bika asbahna wa bika amsayna wa bika nahya wa bika namut.",
            translation = "Ya Allah, dengan-Mu kami memasuki pagi, dengan-Mu kami memasuki petang, dengan-Mu kami hidup, dan dengan-Mu kami mati."
        ),
        DailyDua(
            id = "morning_hayy",
            category = "Morning",
            title = "Ya Hayyu Ya Qayyum",
            arabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ",
            transliteration = "Ya Hayyu ya Qayyum, bi rahmatika astaghith, aslih li sha'ni kullahu.",
            translation = "Wahai Dzat Yang Maha Hidup dan Maha Menegakkan, dengan rahmat-Mu aku memohon pertolongan. Perbaikilah seluruh urusanku."
        ),
        DailyDua(
            id = "evening_kingdom",
            category = "Evening",
            title = "Evening Remembrance",
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ",
            transliteration = "Amsayna wa amsal mulku lillah, walhamdu lillah.",
            translation = "Kami memasuki petang dan kerajaan seluruhnya milik Allah, segala puji bagi Allah."
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
            id = "evening_protection",
            category = "Evening",
            title = "Protection at Night",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'udhu bi kalimatillahit tammati min sharri ma khalaq.",
            translation = "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari keburukan makhluk yang Dia ciptakan."
        ),
        DailyDua(
            id = "before_sleep",
            category = "Daily Activities",
            title = "Before Sleep",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya.",
            translation = "Dengan nama-Mu ya Allah aku mati dan aku hidup."
        ),
        DailyDua(
            id = "wake_up",
            category = "Daily Activities",
            title = "When Waking Up",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا",
            transliteration = "Alhamdulillahil ladhi ahyana ba'da ma amatana.",
            translation = "Segala puji bagi Allah yang telah menghidupkan kami setelah mematikan kami."
        ),
        DailyDua(
            id = "leaving_home",
            category = "Daily Activities",
            title = "Leaving Home",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillah, tawakkaltu 'alallah, la hawla wa la quwwata illa billah.",
            translation = "Dengan nama Allah, aku bertawakal kepada Allah. Tiada daya dan kekuatan kecuali dengan pertolongan Allah."
        ),
        DailyDua(
            id = "entering_home",
            category = "Daily Activities",
            title = "Entering Home",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ الْمَوْلِجِ وَخَيْرَ الْمَخْرَجِ",
            transliteration = "Allahumma inni as'aluka khayral mawliji wa khayral makhraj.",
            translation = "Ya Allah, aku memohon kepada-Mu sebaik-baik tempat masuk dan sebaik-baik tempat keluar."
        ),
        DailyDua(
            id = "before_eating",
            category = "Daily Activities",
            title = "Before Eating",
            arabic = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah.",
            translation = "Dengan nama Allah."
        ),
        DailyDua(
            id = "after_eating",
            category = "Daily Activities",
            title = "After Eating",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ",
            transliteration = "Alhamdulillahil ladhi at'amani hadha wa razaqanihi.",
            translation = "Segala puji bagi Allah yang telah memberiku makanan ini dan menganugerahkannya kepadaku."
        ),
        DailyDua(
            id = "travel",
            category = "Daily Activities",
            title = "Travel Dua",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ",
            transliteration = "Subhanalladhi sakhkhara lana hadha wa ma kunna lahu muqrinin.",
            translation = "Maha Suci Allah yang telah menundukkan kendaraan ini untuk kami, padahal kami tidak mampu menguasainya."
        ),
        DailyDua(
            id = "after_wudhu",
            category = "Prayer",
            title = "After Wudhu",
            arabic = "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Ashhadu an la ilaha illallah wahdahu la sharika lah.",
            translation = "Aku bersaksi bahwa tidak ada ilah selain Allah semata, tidak ada sekutu bagi-Nya."
        ),
        DailyDua(
            id = "after_adhan",
            category = "Prayer",
            title = "After Adhan",
            arabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلَاةِ الْقَائِمَةِ",
            transliteration = "Allahumma rabba hadhihid-da'watit-tammah was-salatil qa'imah.",
            translation = "Ya Allah, Tuhan pemilik seruan yang sempurna ini dan shalat yang akan ditegakkan."
        ),
        DailyDua(
            id = "entering_masjid",
            category = "Prayer",
            title = "Entering the Mosque",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allahummaftah li abwaba rahmatik.",
            translation = "Ya Allah, bukakanlah bagiku pintu-pintu rahmat-Mu."
        ),
        DailyDua(
            id = "leaving_masjid",
            category = "Prayer",
            title = "Leaving the Mosque",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allahumma inni as'aluka min fadlik.",
            translation = "Ya Allah, aku memohon karunia-Mu."
        ),
        DailyDua(
            id = "seeking_knowledge",
            category = "Learning",
            title = "Seeking Knowledge",
            arabic = "رَبِّ زِدْنِي عِلْمًا",
            transliteration = "Rabbi zidni 'ilma.",
            translation = "Ya Tuhanku, tambahkanlah ilmu kepadaku."
        ),
        DailyDua(
            id = "ease_affairs",
            category = "Learning",
            title = "Ease My Affairs",
            arabic = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
            transliteration = "Rabbi ishrah li sadri wa yassir li amri.",
            translation = "Ya Tuhanku, lapangkanlah dadaku dan mudahkanlah urusanku."
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
