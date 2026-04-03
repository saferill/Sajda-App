package com.sajda.app.ui.screen

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajda.app.ui.component.SajdaScrollColumn
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerLow

@Composable
fun OverlayShell(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SajdaScrollColumn {
        SajdaTopBar(
            title = title,
            subtitle = subtitle,
            leading = {
                SajdaTopAction(Icons.Rounded.ArrowBack, "Kembali", onBack)
            },
            trailing = trailing
        )
        content()
    }
}

@Composable
fun EmptyStateCard(title: String, message: String) {
    SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SanctuaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

fun showTimePicker(
    context: Context,
    initialTime: String,
    onPicked: (String) -> Unit
) {
    val (initialHour, initialMinute) = initialTime
        .split(":")
        .mapIndexed { index, value ->
            value.toIntOrNull() ?: if (index == 0) 20 else 0
        }
        .let { parts -> (parts.getOrElse(0) { 20 }) to (parts.getOrElse(1) { 0 }) }

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onPicked("%02d:%02d".format(hourOfDay, minute))
        },
        initialHour,
        initialMinute,
        true
    ).show()
}
