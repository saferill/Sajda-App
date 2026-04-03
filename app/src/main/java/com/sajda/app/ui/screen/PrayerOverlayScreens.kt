package com.sajda.app.ui.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.PrayerTimeCalculator
import com.sajda.app.util.displayName
import com.sajda.app.util.localizedPrayerName
import com.sajda.app.util.pick
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyPrayerScheduleScreen(
    weeklyPrayerTimes: List<PrayerTime>,
    monthlyPrayerTimes: List<PrayerTime>,
    settings: UserSettings,
    onBack: () -> Unit
) {
    var selectedRangeDays by rememberSaveable { mutableIntStateOf(7) }
    val prayerTimes = if (selectedRangeDays == 30) monthlyPrayerTimes else weeklyPrayerTimes

    OverlayShell(
        title = settings.pick("Jadwal Sholat", "Prayer Schedule"),
        subtitle = if (selectedRangeDays == 30) {
            settings.pick("30 hari ke depan", "Next 30 days")
        } else {
            settings.pick("7 hari ke depan", "Next 7 days")
        },
        onBack = onBack
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceChip(
                label = settings.pick("7 Hari", "7 Days"),
                selected = selectedRangeDays == 7,
                onClick = { selectedRangeDays = 7 }
            )
            ChoiceChip(
                label = settings.pick("30 Hari", "30 Days"),
                selected = selectedRangeDays == 30,
                onClick = { selectedRangeDays = 30 }
            )
        }

        prayerTimes.forEach { prayerTime ->
            val details = PrayerTimeCalculator.calculateDetailedPrayerTimes(
                date = LocalDate.parse(prayerTime.date),
                latitude = prayerTime.latitude,
                longitude = prayerTime.longitude,
                locationName = prayerTime.locationName,
                calculationMethod = settings.prayerCalculationMethod,
                asrMadhhab = settings.asrMadhhab
            )
            SanctuaryCard {
                Text(
                    text = DateTimeUtils.formatDateLabel(prayerTime.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = settings.pick(
                        "Imsak ${details.imsak} - Terbit ${details.sunrise}",
                        "Imsak ${details.imsak} - Sunrise ${details.sunrise}"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WeeklyPrayerSlot(PrayerName.FAJR.displayName(settings.appLanguage), prayerTime.fajr)
                    WeeklyPrayerSlot(PrayerName.DHUHR.displayName(settings.appLanguage), prayerTime.dhuhr)
                    WeeklyPrayerSlot(PrayerName.ASR.displayName(settings.appLanguage), prayerTime.asr)
                    WeeklyPrayerSlot(PrayerName.MAGHRIB.displayName(settings.appLanguage), prayerTime.maghrib)
                    WeeklyPrayerSlot(PrayerName.ISHA.displayName(settings.appLanguage), prayerTime.isha)
                }
            }
        }
    }
}

@Composable
private fun WeeklyPrayerSlot(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WorshipProgressScreen(
    settings: UserSettings,
    onBack: () -> Unit
) {
    val progress = (settings.dailyAyatRead / 20f).coerceIn(0f, 1f)

    OverlayShell(
        title = settings.pick("Progres Ibadah", "Worship Progress"),
        subtitle = settings.pick("Lacak ibadah harian", "Track daily worship"),
        onBack = onBack
    ) {
        HeroCard {
            Text(
                text = settings.pick("${settings.streakCount} Hari Berturut-turut", "${settings.streakCount} Day Streak"),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
            )
            Text(
                text = settings.pick("${settings.dailyAyatRead} ayat dibaca hari ini", "${settings.dailyAyatRead} verses read today"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        SanctuaryCard {
            Text(
                text = settings.pick("Ringkasan pekanan", "Weekly summary"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            repeat(7) { index ->
                val bars = ((settings.dailyAyatRead + index * 2) % 20).coerceAtLeast(3)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = settings.pick("Hari ${index + 1}", "Day ${index + 1}"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = bars / 20f,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QiblaScreen(
    prayerTime: PrayerTime?,
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val direction = prayerTime?.qiblaDirection ?: 294.0
    val compassState = rememberCompassState()
    val qiblaRotation = ((direction.toFloat() - compassState.heading) + 360f) % 360f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SajdaTopBar(
            title = appLanguage.pick("Kiblat", "Qibla"),
            subtitle = prayerTime?.locationName,
            leading = {
                SajdaTopAction(Icons.Rounded.ArrowBack, appLanguage.pick("Kembali", "Back"), onBack)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    CompassFace(
                        modifier = Modifier
                            .size(260.dp)
                            .rotate(-compassState.heading)
                    )
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = appLanguage.pick("Kiblat", "Qibla"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(128.dp)
                            .rotate(qiblaRotation)
                    )
                }

                Text(
                    text = appLanguage.pick("${direction.toInt()} derajat", "${direction.toInt()} degrees"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetadataChip(
                        text = appLanguage.pick(
                            "Arah kiblat ${direction.toInt()} derajat",
                            "Qibla ${direction.toInt()} degrees"
                        ),
                        active = true
                    )
                    MetadataChip(
                        text = appLanguage.pick(
                            "Arah kompas ${compassState.heading.toInt()} derajat",
                            "Heading ${compassState.heading.toInt()} degrees"
                        )
                    )
                    MetadataChip(
                        text = when (compassState.accuracy) {
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> appLanguage.pick("Akurasi tinggi", "High accuracy")
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> appLanguage.pick("Akurasi sedang", "Medium accuracy")
                            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> appLanguage.pick("Perlu kalibrasi", "Needs calibration")
                            else -> appLanguage.pick("Akurasi belum terbaca", "Accuracy unavailable")
                        }
                    )
                }
            }
        }

        Text(
            text = if (compassState.isAvailable) {
                if (compassState.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                    appLanguage.pick(
                        "Kalibrasi kompas dengan gerakan angka delapan dan jauhkan ponsel dari logam.",
                        "Calibrate the compass with a figure-eight motion and keep the phone away from metal."
                    )
                } else {
                    appLanguage.pick(
                        "Pegang ponsel datar dan putar perlahan sampai panah mengarah ke kiblat.",
                        "Hold the phone flat and turn slowly until the arrow points to the qibla."
                    )
                }
            } else {
                appLanguage.pick(
                    "Sensor kompas tidak tersedia, jadi aplikasi hanya menampilkan arah kiblat statis.",
                    "Compass sensors are unavailable, so the app can only show a static qibla direction."
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
/*
        SanctuaryCard {
            Text(
                text = "Fallback manual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kalau sensor kompas lemah, hadapkan sisi atas ponsel ke ${direction.toInt()}° dari utara. Putar badan perlahan sampai heading mendekati nilai itu.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
*/

@Composable
private fun CompassFace(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(220.dp)) {
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = size.minDimension / 2f
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.24f),
            radius = size.minDimension / 2.6f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(size.width / 2f, 12f),
            end = Offset(size.width / 2f, size.height - 12f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(12f, size.height / 2f),
            end = Offset(size.width - 12f, size.height / 2f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
    }
}

private data class CompassState(
    val heading: Float,
    val isAvailable: Boolean,
    val accuracy: Int
)

@Composable
private fun rememberCompassState(): CompassState {
    val context = LocalContext.current
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val rotationSensor = remember(sensorManager) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }
    var headingDegrees by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }

    DisposableEffect(sensorManager, rotationSensor) {
        if (rotationSensor == null) {
            return@DisposableEffect onDispose { }
        }

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuth < 0f) {
                    azimuth += 360f
                }
                headingDegrees = azimuth
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return CompassState(
        heading = headingDegrees,
        isAvailable = rotationSensor != null,
        accuracy = sensorAccuracy
    )
}

@Composable
fun BackgroundAudioInfoScreen(
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    OverlayShell(
        title = appLanguage.pick("Audio Latar", "Background Audio"),
        subtitle = appLanguage.pick("Layanan playback", "Playback service"),
        onBack = onBack
    ) {
        SanctuaryCard {
            Text(
                text = appLanguage.pick(
                    "Audio murattal berjalan melalui foreground service, jadi playback tetap hidup saat aplikasi ditutup atau Anda pindah ke aplikasi lain.",
                    "Murattal audio runs through a foreground service, so playback stays alive when the app is closed or you switch to another app."
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = appLanguage.pick(
                    "Kontrol utama tersedia dari notifikasi dan mini player Sajda.",
                    "Main controls remain available from the notification and the Sajda mini player."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WidgetPreviewScreen(
    prayerTime: PrayerTime?,
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    OverlayShell(
        title = appLanguage.pick("Pratinjau Widget", "Widget Preview"),
        subtitle = appLanguage.pick("Kartu waktu sholat", "Prayer time cards"),
        onBack = onBack
    ) {
        HeroCard {
            Text(
                text = prayerTime?.locationName ?: "Jakarta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${localizedPrayerName(PrayerName.FAJR.label, appLanguage)} ${prayerTime?.fajr ?: "--:--"} | ${localizedPrayerName(PrayerName.DHUHR.label, appLanguage)} ${prayerTime?.dhuhr ?: "--:--"} | ${localizedPrayerName(PrayerName.MAGHRIB.label, appLanguage)} ${prayerTime?.maghrib ?: "--:--"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
            )
        }
        SanctuaryCard {
            Text(
                text = appLanguage.pick(
                    "Mini widget ini dirancang untuk tampilan homescreen: ringkas, jelas, dan fokus pada jadwal sholat berikutnya.",
                    "This mini widget is designed for the home screen: compact, clear, and focused on the next prayer time."
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EmptyStateScreen(
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    OverlayShell(
        title = appLanguage.pick("Keadaan Kosong", "Empty States"),
        subtitle = appLanguage.pick("Tampilan cadangan yang ramah", "Friendly fallback views"),
        onBack = onBack
    ) {
        EmptyStateCard(
            title = appLanguage.pick("Belum ada bookmark", "No bookmarks yet"),
            message = appLanguage.pick(
                "Simpan ayat yang ingin Anda kunjungi lagi nanti.",
                "Save the verses you want to revisit later."
            )
        )
        EmptyStateCard(
            title = appLanguage.pick("Belum ada audio offline", "No offline audio yet"),
            message = appLanguage.pick(
                "Unduh murattal per-surah agar penyimpanan tetap ringan.",
                "Download murattal by surah to keep storage light."
            )
        )
        EmptyStateCard(
            title = appLanguage.pick("Belum ada hasil pencarian", "No search results yet"),
            message = appLanguage.pick(
                "Coba kata yang lebih singkat atau cari dari nama surah.",
                "Try a shorter keyword or search by surah name."
            )
        )
    }
}
