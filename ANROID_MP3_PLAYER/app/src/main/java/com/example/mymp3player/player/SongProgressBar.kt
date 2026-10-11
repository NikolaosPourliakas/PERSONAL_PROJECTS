package com.example.mymp3player.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.mymp3player.themes.themes_setup.CyberTargets
import java.util.Locale

@Composable
fun SongProgressBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    trackColor: Color,
    thumbColor: Color,
    timeTextColor: Color
) {
    val safeDuration = duration.coerceAtLeast(1L)

    val progress = (position.toFloat() / safeDuration.toFloat()).coerceIn(0f,1f)

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    CyberTargets.setTarget(
                        id = CyberTargets.PROGRESS_BAR,
                        rect = coordinates.boundsInRoot()
                    )
                }
    ) {
        Slider(
            value = progress,
            onValueChange = { value ->
                val newPosition =(value * safeDuration).toLong()
                onSeek(newPosition)
            },
            valueRange = 0f..1f,
            colors =
                SliderDefaults.colors(
                    thumbColor = thumbColor,
                    activeTrackColor = thumbColor,
                    inactiveTrackColor = trackColor
                ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(position),
                color = timeTextColor,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = formatTime(duration),
                color = timeTextColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Suppress("DefaultLocale")
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds.coerceAtLeast(0L) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}