# Sajda App Roadmap & Detailed TODO

Dokumen ini dipakai sebagai arah kerja utama untuk pengembangan Sajda App ke versi yang lebih matang, stabil, dan siap dipakai publik. Fokus utamanya bukan hanya menambah fitur, tetapi memastikan pengalaman ibadah terasa tenang, akurat, konsisten, dan dapat dipercaya di penggunaan harian.

## Cara Membaca Dokumen Ini

- Item dengan target `v1.2` adalah prioritas terdekat dan paling penting.
- Item dengan target `v1.3` adalah peningkatan besar setelah fondasi utama lebih stabil.
- Item `Long-Term` adalah arah jangka panjang menuju aplikasi yang benar-benar siap distribusi luas.
- Setiap poin dilengkapi penjelasan singkat agar pengerjaan tidak kehilangan konteks produk.

---

## v1.2 Priority

Fase ini fokus pada fondasi kepercayaan aplikasi: adzan harus reliabel, konten Qur'an harus lebih matang, dan pengguna harus lebih mudah memahami kenapa suatu fitur bekerja atau gagal bekerja.

### Adhan Reliability

- [x] Add internal adhan event logs

  Penjelasan: Aplikasi perlu menyimpan jejak teknis setiap event penting seperti alarm dijadwalkan, alarm terpanggil, service dimulai, audio diputar, audio gagal, stop manual, dan snooze. Tanpa log seperti ini, masalah adzan akan sulit ditelusuri karena kita hanya bisa menebak-nebak apakah masalahnya ada di alarm, receiver, service, audio, atau perangkat.

  Hasil akhir yang diharapkan: Tersedia histori teknis yang jelas sehingga kalau adzan tidak bunyi, kita bisa tahu persis titik gagalnya.

- [x] Add manual adhan test for all prayer times

  Penjelasan: Saat ini tes manual masih belum mewakili semua waktu sholat secara lengkap. Pengguna perlu bisa menguji Subuh, Dzuhur, Ashar, Maghrib, dan Isya secara terpisah karena perilaku audio atau penjadwalannya bisa berbeda. Ini penting terutama untuk membedakan audio Subuh dengan audio adzan reguler.

  Hasil akhir yang diharapkan: Pengguna dapat mengetes semua jenis adzan tanpa menunggu jam sholat asli.

- [x] Add fallback short alert sound if full adhan audio fails

  Penjelasan: Kadang audio adzan penuh gagal diputar karena file rusak, resource tidak terbaca, fokus audio terganggu, atau kebijakan sistem perangkat. Dalam situasi seperti itu, aplikasi tetap harus memberikan sinyal minimal agar pengguna tahu waktu sholat sudah masuk. Fallback bunyi pendek akan menjaga fungsi inti aplikasi tetap berjalan walau konten audio utama bermasalah.

  Hasil akhir yang diharapkan: Adzan tidak benar-benar “diam total” saat file utama gagal diputar.

- [x] Add 7-day adhan history

  Penjelasan: Riwayat 7 hari akan sangat membantu untuk validasi penggunaan nyata. Pengguna bisa melihat apakah Subuh, Dzuhur, Ashar, Maghrib, dan Isya benar-benar terpanggil pada hari-hari sebelumnya. Ini juga berguna saat menguji stabilitas di berbagai merek HP.

  Hasil akhir yang diharapkan: Ada halaman atau kartu riwayat yang menunjukkan jadwal adzan yang sukses, gagal, disnooze, atau dihentikan.

- [x] Show detailed next scheduled alarm info

  Penjelasan: Informasi “alarm berikutnya” sebaiknya tidak hanya berisi nama sholat dan jam, tetapi juga tanggal, lokasi, metode hisab, status exact alarm, dan status pengaturan penting lain yang memengaruhi keandalan alarm. Ini membuat aplikasi terasa lebih transparan dan profesional.

  Hasil akhir yang diharapkan: Pengguna bisa memverifikasi dengan cepat kapan alarm berikutnya akan benar-benar berbunyi dan dalam kondisi sistem seperti apa.

- [x] Improve vendor-specific guidance for battery optimization and auto start

  Penjelasan: Banyak masalah adzan di Android bukan karena bug kode, tetapi karena aturan vendor seperti Xiaomi, Oppo, Vivo, Realme, atau Samsung yang mematikan proses background. Aplikasi perlu memberi panduan yang lebih spesifik per vendor, bukan hanya pengingat umum tentang battery optimization.

  Hasil akhir yang diharapkan: Pengguna lebih mudah mengaktifkan pengaturan yang benar di perangkat mereka sendiri.

- [x] Improve exact alarm recovery after reboot, date change, timezone change, and idle mode

  Penjelasan: Alur recovery sudah ada, tetapi masih perlu terus diperkuat agar benar-benar tahan terhadap restart perangkat, perubahan zona waktu, perubahan jam manual, device idle, dan perilaku sistem ketika aplikasi jarang dibuka. Ini inti dari pengalaman aplikasi adzan yang serius.

  Hasil akhir yang diharapkan: Jadwal adzan tetap pulih otomatis walau perangkat mengalami perubahan sistem penting.

### Adhan Diagnostics UI

- [x] Add a dedicated “Why adhan is not working” help section

  Penjelasan: Banyak pengguna tidak memahami apakah masalahnya ada pada izin notifikasi, exact alarm, volume alarm, mode senyap, battery optimization, atau autostart. Halaman bantuan khusus akan mengurangi kebingungan dan mempercepat penyelesaian masalah tanpa harus keluar dari aplikasi.

  Hasil akhir yang diharapkan: Pengguna bisa melakukan diagnosis mandiri dengan panduan yang mudah dipahami.

- [x] Add quick actions for notification settings, alarm settings, battery settings

  Penjelasan: Menampilkan status saja belum cukup. Pengguna perlu tombol cepat yang langsung membawa mereka ke pengaturan yang relevan. Ini memperpendek perjalanan pengguna dan meningkatkan peluang masalah benar-benar diperbaiki.

  Hasil akhir yang diharapkan: Proses troubleshooting menjadi lebih cepat dan lebih sedikit langkah.

- [x] Add device-specific tips for Xiaomi, Oppo, Vivo, Samsung, Realme

  Penjelasan: Setiap vendor punya nama menu dan perilaku sistem yang berbeda. Menyediakan panduan yang spesifik akan membuat aplikasi terasa lebih memahami masalah dunia nyata, bukan hanya memberi saran generik.

  Hasil akhir yang diharapkan: Tingkat keberhasilan adzan di perangkat populer meningkat karena panduan lebih relevan.

### Qur'an Experience

- [x] Add full English translation support

  Penjelasan: Saat ini bahasa aplikasi sudah punya fondasi, tetapi pengalaman Qur'an dalam bahasa Inggris perlu benar-benar lengkap supaya fitur multi-language tidak terasa setengah jadi. Ini penting untuk memperluas jangkauan pengguna dan membuat pengaturan bahasa punya dampak nyata.

  Hasil akhir yang diharapkan: Seluruh pengalaman baca Qur'an bisa berjalan baik untuk pengguna Indonesia maupun English-speaking users.

- [x] Add reading modes: Arabic only, Arabic + Indonesian, Arabic + English, All

  Penjelasan: Mode baca yang jelas akan membuat pengalaman tilawah lebih fleksibel. Sebagian pengguna ingin fokus hanya pada teks Arab, sebagian ingin membaca bersama terjemahan Indonesia, dan sebagian lagi ingin belajar dengan English translation juga. Pemisahan mode ini membuat fitur bahasa terasa rapi.

  Hasil akhir yang diharapkan: Pengguna bisa memilih cara membaca yang paling sesuai dengan kebutuhan ibadah atau belajar mereka.

- [x] Improve transliteration toggle behavior

  Penjelasan: Transliteration sebaiknya tidak terasa seperti elemen tambahan yang berdiri sendiri. Perilakunya perlu konsisten dengan mode bahasa dan mode baca, supaya tidak membingungkan ketika translation dimatikan atau Arabic only aktif.

  Hasil akhir yang diharapkan: Toggle transliteration menjadi intuitif dan tidak menimbulkan konflik tampilan.

- [x] Improve ayah layout consistency and readability

  Penjelasan: Layar bacaan Al-Qur'an adalah inti aplikasi. Spacing, ukuran teks, urutan elemen, dan ritme visual antarayat harus terasa tenang dan mudah dibaca lama-lama. Ini bagian yang sangat menentukan apakah aplikasi terasa “premium” atau belum.

  Hasil akhir yang diharapkan: Pengalaman membaca lebih nyaman, lebih fokus, dan lebih halus secara visual.

### Spiritual Content

- [x] Add more daily duas

  Penjelasan: Konten doa sekarang sudah ada fondasinya, tetapi masih bisa dibuat jauh lebih kaya. Pengguna akan lebih terbantu jika doa harian mencakup lebih banyak situasi, seperti perlindungan, syukur, kesulitan, safar, keluarga, rezeki, dan ibadah harian lain.

  Hasil akhir yang diharapkan: Halaman doa terasa bernilai tinggi dan berguna setiap hari.

- [x] Add hadith categories

  Penjelasan: Hadith of the Day adalah langkah awal yang bagus, tetapi pengguna juga perlu akses berdasarkan tema agar kontennya lebih bermakna. Kategori seperti niat, shalat, akhlak, sabar, syukur, dan ilmu akan membuat fitur hadits terasa lebih terstruktur.

  Hasil akhir yang diharapkan: Pengguna bisa menjelajah hadits dengan cara yang lebih relevan dan tidak acak semata.

- [x] Add source labels for hadith and dua content

  Penjelasan: Aplikasi ibadah sangat bergantung pada kepercayaan. Karena itu, setiap konten spiritual sebaiknya punya label sumber atau referensi yang jelas. Hal ini akan meningkatkan rasa aman dan keyakinan pengguna terhadap materi yang mereka baca.

  Hasil akhir yang diharapkan: Konten terasa lebih kredibel dan lebih layak dijadikan rujukan ringan harian.

- [x] Expand lightweight tafsir content carefully

  Penjelasan: Tafsir ringan sangat berguna, tetapi harus ditambah dengan hati-hati agar tidak terasa asal rangkum atau terlalu bebas. Fokusnya bukan membuat tafsir yang sangat panjang, tetapi penjelasan singkat yang aman, jelas, dan tetap membantu pengguna memahami ayat.

  Hasil akhir yang diharapkan: Fitur tafsir menjadi lebih bermanfaat tanpa kehilangan kesederhanaan.

---

## v1.4 Priority

Fase ini berfokus pada peningkatan kualitas pengalaman penggunaan. Setelah fondasi lebih stabil, Sajda App perlu terasa lebih kaya, lebih nyaman, dan lebih kuat untuk pemakaian jangka panjang.

### Audio Experience

- [ ] Add multiple qari options

  Penjelasan: Pilihan qari akan membuat pengalaman audio lebih personal. Setiap pengguna punya preferensi sendiri terhadap gaya murattal, jadi memberi pilihan akan meningkatkan rasa memiliki terhadap aplikasi.

  Hasil akhir yang diharapkan: Pengguna dapat memilih qari yang paling mereka sukai tanpa meninggalkan ekosistem aplikasi.

- [ ] Add retry download flow

  Penjelasan: Download audio per-surah adalah fitur penting, dan kegagalan download tidak boleh berakhir buntu. Sistem retry akan membantu ketika jaringan lemah atau server sempat tidak responsif.

  Hasil akhir yang diharapkan: Pengalaman download audio terasa lebih tahan gangguan dan tidak mudah gagal permanen.

- [ ] Add mirror audio source support

  Penjelasan: Bergantung pada satu sumber audio membuat fitur rentan jika server sedang lambat atau down. Dengan beberapa mirror, aplikasi bisa tetap melayani pengguna dengan lebih andal.

  Hasil akhir yang diharapkan: Tingkat keberhasilan unduhan meningkat dan audio lebih tersedia dalam jangka panjang.

- [ ] Add repeat ayah mode

  Penjelasan: Repeat ayat berguna untuk hafalan, tadabbur, dan belajar pelafalan. Ini fitur kecil tetapi sangat kuat untuk meningkatkan kualitas penggunaan harian.

  Hasil akhir yang diharapkan: Aplikasi lebih berguna bukan hanya untuk mendengar, tetapi juga untuk mengulang dan menghafal.

- [ ] Add sleep timer

  Penjelasan: Sebagian pengguna mendengarkan murattal sebelum tidur atau saat istirahat. Sleep timer membantu menjaga kenyamanan dan baterai tanpa memaksa pengguna menghentikan audio secara manual.

  Hasil akhir yang diharapkan: Pengalaman audio menjadi lebih nyaman dan fleksibel.

- [ ] Add auto-scroll during playback

  Penjelasan: Saat audio berjalan, tampilan ayat sebaiknya mengikuti bacaan agar pengalaman terasa sinkron. Ini akan membuat sesi tilawah jauh lebih hidup dan premium.

  Hasil akhir yang diharapkan: Pengguna dapat membaca sambil mendengarkan tanpa harus terus scroll manual.

### Qibla

- [ ] Add sensor quality indicator

  Penjelasan: Masalah kompas sering datang dari sensor perangkat, bukan dari logika aplikasi. Pengguna perlu tahu apakah hasil arah kiblat bisa dipercaya atau sedang terganggu.

  Hasil akhir yang diharapkan: Pengguna memahami kualitas hasil kompas sebelum mengandalkannya.

- [ ] Add compass calibration guide

  Penjelasan: Banyak pengguna tidak tahu cara mengkalibrasi sensor kompas. Panduan sederhana di dalam aplikasi akan membantu meningkatkan akurasi secara praktis.

  Hasil akhir yang diharapkan: Arah kiblat lebih akurat di penggunaan nyata.

- [ ] Add fallback map-based qibla direction

  Penjelasan: Jika sensor kompas buruk, aplikasi tetap perlu memberi alternatif. Arah kiblat berbasis peta atau bearing statis akan membantu sebagai fallback yang lebih aman.

  Hasil akhir yang diharapkan: Fitur kiblat tetap berguna meskipun kualitas sensor perangkat rendah.

### Worship Progress

- [ ] Add daily ayah target

  Penjelasan: Target ayat harian membantu membangun kebiasaan tanpa membuat pengguna merasa terbebani. Ini langkah awal yang bagus untuk memperkuat engagement.

  Hasil akhir yang diharapkan: Pengguna lebih terdorong membaca Qur'an secara konsisten setiap hari.

- [ ] Add khatam target

  Penjelasan: Banyak pengguna ingin punya tujuan jangka panjang, bukan hanya target harian. Target khatam akan memberi rasa perjalanan dan pencapaian yang lebih besar.

  Hasil akhir yang diharapkan: Progress ibadah terasa lebih terarah dan memotivasi.

- [ ] Add weekly and monthly reading statistics

  Penjelasan: Statistik mingguan dan bulanan akan membantu pengguna melihat pola ibadah mereka dengan lebih jelas. Ini lebih bermakna daripada sekadar angka harian.

  Hasil akhir yang diharapkan: Pengguna bisa melihat perkembangan nyata dan menjaga konsistensi.

- [ ] Improve home progress summary

  Penjelasan: Ringkasan progres di home harus terasa hidup, singkat, dan relevan. Ini titik penting untuk membuat layar utama lebih personal dan tidak sekadar statis.

  Hasil akhir yang diharapkan: Home screen menjadi lebih personal dan lebih memotivasi.

### Backup

- [ ] Add bookmark backup/restore

  Penjelasan: Bookmark Qur'an punya nilai pribadi yang tinggi. Kehilangannya saat ganti perangkat atau reinstall akan sangat merugikan pengguna.

  Hasil akhir yang diharapkan: Bookmark tetap aman dan bisa dipulihkan.

- [ ] Add settings backup/restore

  Penjelasan: Pengaturan adzan, lokasi, mode baca, dan preferensi lain sebaiknya tidak harus diatur ulang dari nol saat pindah perangkat. Backup settings akan membuat aplikasi terasa lebih matang.

  Hasil akhir yang diharapkan: Pengalaman migrasi pengguna menjadi jauh lebih nyaman.

---

## Long-Term

Bagian ini adalah arah penguatan produk ke level publik yang lebih serius. Tidak semuanya harus dikerjakan cepat, tetapi penting untuk disiapkan sejak sekarang agar arah pengembangan tetap konsisten.

### Public Readiness

- [ ] Add LICENSE

  Penjelasan: Repository publik perlu lisensi yang jelas agar orang tahu bagaimana kode boleh digunakan, dimodifikasi, atau didistribusikan. Tanpa lisensi, secara hukum hak penggunaan pihak lain jadi tidak jelas.

  Hasil akhir yang diharapkan: Repo terlihat lebih profesional dan aman untuk kolaborasi publik.

- [ ] Add privacy policy

  Penjelasan: Aplikasi yang menggunakan lokasi, notifikasi, dan mekanisme update perlu punya penjelasan privasi yang jelas. Ini penting untuk kepercayaan pengguna dan juga untuk kesiapan distribusi resmi.

  Hasil akhir yang diharapkan: Pengguna memahami data apa yang dipakai dan untuk tujuan apa.

- [ ] Add FAQ/help page

  Penjelasan: Pertanyaan seperti “kenapa adzan tidak bunyi”, “kenapa qibla tidak akurat”, atau “bagaimana download audio” akan selalu muncul. FAQ akan mengurangi kebingungan dan beban support.

  Hasil akhir yang diharapkan: Pengguna lebih mandiri dan aplikasi terasa lebih siap dipakai publik.

- [ ] Add GitHub issue template

  Penjelasan: Template issue membantu pengguna dan kontributor melaporkan bug dengan informasi yang lengkap, terutama untuk bug teknis seperti adzan yang tidak berjalan di perangkat tertentu.

  Hasil akhir yang diharapkan: Laporan masalah lebih rapi dan lebih mudah ditindaklanjuti.

- [ ] Add GitHub pull request template

  Penjelasan: Kalau nanti project ini berkembang dan menerima kontribusi, PR template akan membantu menjaga kualitas perubahan dan konsistensi review.

  Hasil akhir yang diharapkan: Proses kolaborasi jadi lebih profesional dan efisien.

### Store Readiness

- [ ] Prepare Google Play release setup

  Penjelasan: Distribusi lewat Play Store akan mengurangi warning sideload di banyak perangkat dan memberi jalur update yang lebih resmi. Untuk sampai ke sana, project perlu disiapkan dari sisi metadata, kebijakan, dan packaging.

  Hasil akhir yang diharapkan: Sajda App siap masuk jalur distribusi yang lebih terpercaya.

- [ ] Add crash reporting

  Penjelasan: Saat jumlah pengguna bertambah, crash yang tidak terlihat akan menjadi masalah besar. Crash reporting akan membantu mengetahui titik error yang benar-benar dialami pengguna di dunia nyata.

  Hasil akhir yang diharapkan: Bug kritis lebih cepat diketahui dan diperbaiki.

- [ ] Add lightweight analytics

  Penjelasan: Analytics ringan bisa membantu memahami fitur mana yang paling sering dipakai, halaman mana yang paling penting, dan alur mana yang paling sering gagal. Ini berguna untuk mengambil keputusan produk yang lebih tepat.

  Hasil akhir yang diharapkan: Pengembangan fitur menjadi lebih berbasis data, bukan asumsi semata.

- [ ] Improve in-app update flow

  Penjelasan: Sistem update sekarang sudah ada fondasinya, tetapi masih bisa dibuat lebih halus dan lebih aman. Nantinya alur update harus terasa mudah dipahami pengguna dan tidak mengganggu pengalaman utama.

  Hasil akhir yang diharapkan: Pengguna lebih mudah pindah ke versi terbaru tanpa friksi yang besar.

- [ ] Run accessibility improvements

  Penjelasan: Aplikasi ibadah harus nyaman untuk lebih banyak jenis pengguna, termasuk yang membutuhkan ukuran huruf lebih besar, kontras lebih baik, atau dukungan pembaca layar.

  Hasil akhir yang diharapkan: Aplikasi lebih inklusif dan lebih layak dipakai publik luas.

---

## Recommended Execution Order

Urutan pengerjaan yang paling efisien:

1. Stabilkan adhan dan diagnosis sistemnya.
2. Lengkapi pengalaman Qur'an dengan English translation dan mode baca yang lebih matang.
3. Perdalam konten spiritual: doa, hadits, dan tafsir ringan.
4. Naikkan kualitas pengalaman audio.
5. Rapikan qibla dan progress system.
6. Selesaikan kesiapan publik: policy, template, FAQ, dan store-readiness.

---

## Working Principle

Setiap pengembangan baru sebaiknya diuji dengan pertanyaan berikut:

- Apakah fitur ini benar-benar membantu ibadah harian pengguna?
- Apakah fitur ini tetap berguna saat offline?
- Apakah fitur ini menambah ketenangan, bukan kerumitan?
- Apakah fitur ini cukup stabil untuk dipakai setiap hari?

Kalau jawabannya belum kuat, berarti fitur tersebut masih perlu dipikirkan ulang sebelum masuk fase implementasi besar.
