package com.example.mymp3player.themes.themes_setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.mymp3player.UIController
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.ThemeAnimation
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val PinballColors = listOf(
    Color(0xFFFFD600),
    Color(0xFFFF4081),
    Color(0xFF00E5FF),
    Color(0xFF00E676),
    Color(0xFFFF7043),
    Color(0xFF006EFF),
    Color(0xFFD500F9),
    Color.White
)

object PinballAnimation : ThemeAnimation {

    override val durationMillis: Int = 1800
    override val pauseMillis: Long = 300L

    override fun nextTheme(baseTheme: AppTheme): AppTheme = baseTheme

    @Composable
    fun animatedTheme(baseTheme: AppTheme): AppTheme {
        var colorIndex = 0

        @Composable
        fun animatedColor(original: Color): Color {
            val index = colorIndex++
            val paletteStart = index % PinballColors.size
            val intensity = 0.55f + (index % 5) * 0.1125f
            val duration = (3600f / intensity).toInt().coerceIn(700, 6000)

            val progress = remember(index, original) {
                Animatable(0f)
            }

            LaunchedEffect(index, original, duration) {
                var step = 0

                while (true) {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = duration,easing = LinearEasing)
                    )

                    step++
                    delay((20L + (index % 4) * 10L).milliseconds)
                }
            }

            val cycle = (progress.value * (PinballColors.size - 1))
                .coerceIn(0f, (PinballColors.size - 1).toFloat())

            val firstIndex = (paletteStart + cycle.toInt()) % PinballColors.size
            val secondIndex = (firstIndex + 1) % PinballColors.size
            val fraction = cycle - cycle.toInt()

            val paletteColor = lerp(
                PinballColors[firstIndex],
                PinballColors[secondIndex],
                fraction
            )

            // Συνδυάζει το αρχικό χρώμα με την κινούμενη παλέτα.
            return lerp(original, paletteColor, intensity)
        }

        return baseTheme.copy(
            headerTitleText = animatedColor(baseTheme.headerTitleText),
            headerSongText = animatedColor(baseTheme.headerSongText),
            settingsIcon = animatedColor(baseTheme.settingsIcon),
            sourceButtonColor = animatedColor(baseTheme.sourceButtonColor),
            settingsButtonColor = animatedColor(baseTheme.settingsButtonColor),
            settingsText = animatedColor(baseTheme.settingsText),
            settingsSecondaryText = animatedColor(baseTheme.settingsSecondaryText),
            listText = animatedColor(baseTheme.listText),
            allSongsColor = animatedColor(baseTheme.allSongsColor),
            folderColor = animatedColor(baseTheme.folderColor),
            playlistColor = animatedColor(baseTheme.playlistColor),
            sourceIcon = animatedColor(baseTheme.sourceIcon),
            randomActive = animatedColor(baseTheme.randomActive),
            scrollThumb = animatedColor(baseTheme.scrollThumb),
            songBarProgress = animatedColor(baseTheme.songBarProgress),
            songBarThumb = animatedColor(baseTheme.songBarThumb),
            songTimeText = animatedColor(baseTheme.songTimeText),
            prevIcon = animatedColor(baseTheme.prevIcon),
            playIcon = animatedColor(baseTheme.playIcon),
            nextIcon = animatedColor(baseTheme.nextIcon),
            activeSongColor = animatedColor(baseTheme.activeSongColor),
            headerBorder = animatedColor(baseTheme.headerBorder),
            listBorder = animatedColor(baseTheme.listBorder),
            prevBorder = animatedColor(baseTheme.prevBorder),
            playBorder = animatedColor(baseTheme.playBorder),
            nextBorder = animatedColor(baseTheme.nextBorder)
        )
    }

    @Composable
    fun LightOverlay(uiController: UIController) {
        val state by uiController.state.collectAsState()

        val pinballEnabled =
            state.theme.animation === PinballAnimation &&
                    state.themeAnimationsEnabled.contains("Pinball")

        if (!pinballEnabled) return
    }
}

val PinballTheme = AppTheme(
    screenBackground = Color(0xFF10152B),
    headerBackground = Color(0xFF172B5A),
    headerBorder = Color(0xFFFFD600),
    headerTitleText = Color(0xFFFF4081),
    headerSongText = Color(0xFF00E5FF),
    settingsIcon = Color(0xFFFFD600),
    sourceButtonColor = Color(0xFF00E676),
    settingsButtonColor = Color(0xFFFF4081),
    settingsBackground = Color(0xFF151C3A),
    settingsText = Color.White,
    settingsSecondaryText = Color(0xFF90CAF9),
    listBackground = Color(0xFF1C3470),
    listBorder = Color(0xFF00E5FF),
    listText = Color.White,
    allSongsColor = Color(0xFF00E5FF),
    folderColor = Color(0xFFFFD600),
    playlistColor = Color(0xFFFF4081),
    sourceIcon = Color(0xFF00E676),
    randomActive = Color(0xFFFF4081),
    randomInactive = Color(0xFF5C6380),
    scrollTrack = Color(0xFF202A50),
    scrollThumb = Color(0xFFFFD600),
    songBarTrack = Color(0xFF202A50),
    songBarProgress = Color(0xFF00E676),
    songBarThumb = Color(0xFFFF4081),
    prevBg = Color(0xFF1C3470),
    prevBorder = Color(0xFFFFD600),
    prevIcon = Color(0xFFFF7043),
    playBg = Color(0xFFFF4081),
    playBorder = Color(0xFF00E5FF),
    playIcon = Color.White,
    nextBg = Color(0xFF1C3470),
    nextBorder = Color(0xFFFFD600),
    nextIcon = Color(0xFFFF7043),
    songTimeText = Color(0xFF006EFF),
    activeSongColor = Color(0xFF008CFF),
    animation = PinballAnimation
)