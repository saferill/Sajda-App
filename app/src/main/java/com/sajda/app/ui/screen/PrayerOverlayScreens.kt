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
import com.sajda.app.util.displayNameRes
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
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.prayer_schedule),
        subtitle = if (selectedRangeDays == 30) {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.next_30_days)
        } else {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.next_7_days)
        },
        onBack = onBack
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceChip(
                label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.str_7_days),
                selected = selectedRangeDays == 7,
                onClick = { selectedRangeDays = 7 }
            )
            ChoiceChip(
                label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.str_30_days),
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
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.imsak_details_imsak_sunrise_details_sunr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.FAJR.displayNameRes()), prayerTime.fajr)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.DHUHR.displayNameRes()), prayerTime.dhuhr)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.ASR.displayNameRes()), prayerTime.asr)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.MAGHRIB.displayNameRes()), prayerTime.maghrib)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.ISHA.displayNameRes()), prayerTime.isha)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QiblaScreen(
    prayerTime: PrayerTime?,
    appLanguage: AppLanguage,
    onBack: (() -> Unit)? = null
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
            title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qibla),
            subtitle = prayerTime?.locationName,
            leading = onBack?.let { backAction ->
                {
                    SajdaTopAction(
                        Icons.Rounded.ArrowBack,
                        androidx.compose.ui.res.stringResource(com.sajda.app.R.string.back),
                        backAction
                    )
                }
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
                        contentDescription = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qibla),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(128.dp)
                            .rotate(qiblaRotation)
                    )
                }

                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.direction_toint_degrees),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetadataChip(
                        text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.qibla_direction_toint_degrees),
                        active = true
                    )
                    MetadataChip(
                        text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.heading_compassstate_heading_toint_degre)
                    )
                    MetadataChip(
                        text = when (compassState.accuracy) {
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.high_accuracy)
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.medium_accuracy)
                            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.needs_calibration)
                            else -> androidx.compose.ui.res.stringResource(com.sajda.app.R.string.accuracy_unavailable)
                        }
                    )
                }
            }
        }

        Text(
            text = if (compassState.isAvailable) {
                if (compassState.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.calibrate_the_compass_with_a_figure_eigh)
                } else {
                    androidx.compose.ui.res.stringResource(com.sajda.app.R.string.hold_the_phone_flat_and_turn_slowly_unti)
                }
            } else {
                androidx.compose.ui.res.stringResource(com.sajda.app.R.string.compass_sensors_are_unavailable_so_the_a)
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
