package com.example.mymp3player.player

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymp3player.Song
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.themes_setup.CyberTargets
import com.example.mymp3player.themes.themes_setup.OriginalTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.material.icons.filled.Shuffle

@Composable
fun PlayerHeader(
    currentSong: Song?,
    theme: AppTheme,
    onSettingsClick: () -> Unit,
    isRandom: Boolean,
    onShuffleClick: () -> Unit,
    animateSongTitle: Boolean = true
) {
    val context = LocalContext.current
    val isOriginal = theme == OriginalTheme
    var volumeLevel by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(Unit) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        while (true) {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            volumeLevel =
                if (maxVolume > 0) {
                    currentVolume.toFloat() / maxVolume.toFloat()
                } else {
                    0f
                }

            delay(100.milliseconds)
        }
    }

    val intensity =
        if (isOriginal) {
            volumeLevel.coerceIn(0f, 1f)
        } else {
            0f
        }

    val calmLevel = intensity < 0.35f
    val brightLevel = intensity in 0.35f..<0.70f
    val strongLevel = intensity >= 0.70f
    val infiniteTransition = rememberInfiniteTransition(label = "headerRhythm")

    val pulseProgress by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis =
                        if (calmLevel) {
                            1800
                        } else if (brightLevel) {
                            900
                        } else {
                            450
                        }
                ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "headerPulse"
    )

    val colorIntensity =
        if (isOriginal) {
            when {
                calmLevel -> intensity * 0.25f
                brightLevel -> 0.15f + ((intensity - 0.35f) / 0.35f) * 0.40f
                else -> 0.55f + ((intensity - 0.70f) / 0.30f) * 0.35f
            }.coerceIn(0f, 0.90f)
        } else {
            0f
        }

    fun reactiveColor(color: Color): Color {
        if (!isOriginal) {
            return color
        }

        val stronger =
            lerp(
                color,
                color.copy(
                    red = (color.red * 1.45f).coerceAtMost(1f),
                    green = (color.green * 1.45f).coerceAtMost(1f),
                    blue = (color.blue * 1.45f).coerceAtMost(1f)
                ),
                0.75f
            )

        return lerp(color,stronger,colorIntensity)
    }

    val headerBackground = theme.headerBackground
    val headerBorder = reactiveColor(theme.headerBorder)
    val headerTitle = reactiveColor(theme.headerTitleText)
    val headerSong = reactiveColor(theme.headerSongText)
    val settingsColor = reactiveColor(theme.settingsButtonColor)

    val titleAlpha =
        if (isOriginal) {
            0.90f + pulseProgress * 0.10f
        } else {
            1f
        }

    val songAlpha =
        if (isOriginal) {
            0.85f + pulseProgress * 0.15f
        } else {
            1f
        }

    val headerGlow =
        if (isOriginal && strongLevel) {
            1f + ((intensity - 0.70f) / 0.30f).coerceIn(0f, 1f) * 4f * pulseProgress
        } else {
            8f
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                CyberTargets.setTarget(id = CyberTargets.HEADER,rect = coordinates.boundsInRoot())
            }
            .shadow(headerGlow.dp,RoundedCornerShape(16.dp))
            .border(3.dp,headerBorder,RoundedCornerShape(16.dp))
            .background(headerBackground,RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShuffleClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isRandom) {
                            theme.randomActive
                        } else {
                            theme.randomInactive
                        }
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MP3",
                        style = MaterialTheme.typography.labelLarge,
                        color = headerTitle.copy(alpha = titleAlpha)
                    )
                }
                IconButton(
                    onClick = onSettingsClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = settingsColor
                    )
                }
            }

            Spacer(Modifier.height(1.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentSong?.title ?: "No song selected",
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (animateSongTitle) {
                                Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    spacing = MarqueeSpacing(spacing = 50.dp)
                                )
                            } else {
                                Modifier
                            }
                        ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = headerSong.copy(alpha = songAlpha),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = currentSong?.folderName ?: "",
                    modifier = Modifier.fillMaxWidth(),
                    color = headerSong.copy(alpha = 0.60f + pulseProgress * 0.15f),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}