
package com.example.mymp3player.themes.themes_setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.example.mymp3player.UIController
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.ThemeAnimation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val RockRed = Color(0xFFFF3038)
private val RockAmber = Color(0xFFFFB52E)
private val RockOrange = Color(0xFFFF6938)

private data class RockBeamState(
    val seed: Int,
    val baseX: Float,
    val width: Float,
    val color: Color,
    val speed: Float,
    val phase: Float
) {
    val progress = Animatable(0f)
    var intensity by mutableStateOf(0.5f)
}

object ConcertAnimation : ThemeAnimation {

    override val durationMillis: Int = 7000
    override val pauseMillis: Long = 0L
    override fun nextTheme(baseTheme: AppTheme): AppTheme = baseTheme

    @Composable
    fun StageLights(
        uiController: UIController,
        modifier: Modifier = Modifier
    ) {
        val state by uiController.state.collectAsState()

        if (
            state.theme.animation !== ConcertAnimation ||
            "Concert" !in state.themeAnimationsEnabled
        ) {
            return
        }

        val beams = remember {
            List(9) { index ->
                val random = Random(index * 7919 + 37)

                val color = when (index % 3) {
                    0 -> RockRed
                    1 -> RockAmber
                    else -> RockOrange
                }

                RockBeamState(
                    seed = random.nextInt(),
                    baseX = (index + 0.5f) / 9f,
                    width = 0.055f + random.nextFloat() * 0.07f,
                    color = color,
                    speed = 0.55f + random.nextFloat() * 0.7f,
                    phase = random.nextFloat() * (2f * PI.toFloat())
                )
            }
        }

        LaunchedEffect(beams) {
            coroutineScope {
                beams.forEach { beam ->
                    launch {
                        val random = Random(beam.seed)

                        delay(random.nextLong(0L, 1200L))

                        while (true) {
                            beam.intensity = random.nextFloat().coerceIn(0.25f, 1f)
                            beam.progress.snapTo(0f)
                            val duration = (5200f / beam.speed).toInt().coerceIn(1800, 8500)
                            beam.progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = duration, easing = LinearEasing)
                            )
                        }
                    }
                }
            }
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            val widthPx = size.width
            val heightPx = size.height

            beams.forEach { beam ->
                val progress = beam.progress.value
                val intensity = beam.intensity
                val swing = sin(progress * 2f * PI.toFloat() + beam.phase)

                val centerX = (
                        beam.baseX + swing * 0.13f * beam.speed
                        ).coerceIn(0.02f, 0.98f) * widthPx
                val beamWidth = beam.width * widthPx
                val topX = centerX - beamWidth * 0.28f
                val bottomX = centerX + beamWidth * 0.28f
                val alpha = (0.035f + intensity * 0.10f).coerceIn(0.03f, 0.15f)

                drawLine(
                    color = beam.color.copy(alpha = alpha),
                    start = Offset(topX, 0f),
                    end = Offset(bottomX, heightPx),
                    strokeWidth = beamWidth,
                    blendMode = BlendMode.Screen
                )

                drawLine(
                    color = beam.color.copy(alpha = alpha * 0.75f),
                    start = Offset(topX, 0f),
                    end = Offset(bottomX, heightPx),
                    strokeWidth = beamWidth * 0.32f,
                    blendMode = BlendMode.Screen
                )

                drawCircle(
                    color = beam.color.copy(alpha = alpha * 0.65f),
                    radius = beamWidth * 0.55f,
                    center = Offset(topX, heightPx * 0.06f),
                    blendMode = BlendMode.Screen
                )
            }
        }
    }
}

val ConcertTheme = AppTheme(
    screenBackground = Color(0xFF100807),
    headerBackground = Color(0xFF21100D),
    headerBorder = RockRed,
    headerTitleText = Color(0xFFFFE8D6),
    headerSongText = Color(0xFFFFC6A0),
    settingsIcon = RockAmber,
    sourceButtonColor = RockRed,
    settingsButtonColor = RockAmber,
    settingsBackground = Color(0xFF21100D),
    settingsText = Color(0xFFFFE8D6),
    settingsSecondaryText = Color(0xFFD7A995),
    listBackground = Color(0xFF100807),
    listBorder = Color(0xFF8D3027),
    listText = Color(0xFFFFE8D6),
    activeSongColor = RockAmber,
    allSongsColor = RockAmber,
    folderColor = RockOrange,
    playlistColor = RockRed,
    sourceIcon = RockRed,
    randomActive = RockAmber,
    randomInactive = Color(0xFF78615A),
    scrollTrack = Color(0xFF21100D),
    scrollThumb = RockRed,
    songBarTrack = Color(0xFF38201A),
    songBarProgress = RockRed,
    songBarThumb = RockAmber,
    songTimeText = Color(0xFFFFC6A0),
    prevBg = Color(0xFF21100D),
    prevBorder = RockRed,
    prevIcon = RockAmber,
    playBg = RockRed,
    playBorder = RockAmber,
    playIcon = Color(0xFF100807),
    nextBg = Color(0xFF21100D),
    nextBorder = RockRed,
    nextIcon = RockAmber,
    animation = ConcertAnimation
)
