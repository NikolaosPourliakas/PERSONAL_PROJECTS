package com.example.mymp3player.themes.themes_setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.lerp
import com.example.mymp3player.UIController
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.ThemeAnimation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/* STATE */

private class DiscoSpotState(angle: Float,orbitRadius: Float,colorProgress: Float) {
    val angle = Animatable(angle)
    val orbitRadius = Animatable(orbitRadius)
    val sizeProgress = Animatable(0f)
    val alphaProgress = Animatable(0f)
    val colorProgress = Animatable(colorProgress)
}

/* DISCO ANIMATION*/

object DiscoAnimation : ThemeAnimation {
    override val durationMillis: Int = 6000
    override val pauseMillis: Long = 0L
    override fun nextTheme(baseTheme: AppTheme): AppTheme {
        return baseTheme
    }

    @Composable
    fun LightSpotOverlay(uiController: UIController,modifier: Modifier = Modifier) {

        val state by uiController.state.collectAsState()
        val activeThemeIsDisco = state.theme.animation === DiscoAnimation
        val discoLightingEnabled = state.themeAnimationsEnabled.contains("Disco")

        if (!activeThemeIsDisco || !discoLightingEnabled) {
            return
        }
        val random = remember {
            Random(System.currentTimeMillis())
        }

        val spots = remember {
            List(12) {
                DiscoSpotState(
                    angle = random.nextFloat() * (Math.PI * 2.0).toFloat(),
                    orbitRadius = random.nextFloat().coerceIn(0.18f,0.42f),
                    colorProgress = random.nextFloat()
                )
            }
        }

        /* ANIMATION */

        LaunchedEffect(Unit) {
            coroutineScope {
                spots.forEach { spot ->
                    launch {
                        while (true) {
                            val targetOrbitRadius =random.nextFloat().coerceIn(0.18f,0.42f)
                            val rotationDirection =
                                if (random.nextBoolean()) {
                                    1f
                                } else {
                                    -1f
                                }
                            val angleDistance = random.nextFloat().coerceIn(1.5f,3.5f)
                            val movementDuration = random.nextInt(2200,4200)
                            val targetColorProgress = random.nextFloat()

                            coroutineScope {
                                launch {
                                    spot.angle.animateTo(
                                        targetValue = spot.angle.value + angleDistance * rotationDirection,
                                        animationSpec = tween(durationMillis = movementDuration, easing = LinearEasing)
                                    )
                                }
                                launch {
                                    spot.orbitRadius.animateTo(
                                        targetValue = targetOrbitRadius,
                                        animationSpec = tween(durationMillis = movementDuration,easing = FastOutSlowInEasing)
                                    )
                                }
                                launch {

                                    spot.sizeProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = movementDuration / 2,
                                            easing = FastOutSlowInEasing)
                                    )
                                    spot.sizeProgress.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = movementDuration / 2,
                                            easing = FastOutSlowInEasing)
                                    )
                                }
                                launch {
                                    spot.alphaProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = movementDuration / 2,
                                            easing = FastOutSlowInEasing
                                            )
                                    )
                                    spot.alphaProgress.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = movementDuration / 2,
                                            easing = FastOutSlowInEasing
                                            )
                                    )
                                }
                                launch {
                                    spot.colorProgress.animateTo(
                                        targetValue = targetColorProgress,
                                        animationSpec = tween(
                                                durationMillis = movementDuration,
                                                easing = LinearEasing
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(x = size.width / 2f,y = size.height / 2f)

            spots.forEach { spot ->
                val radiusX = size.width * spot.orbitRadius.value
                val radiusY = size.height * spot.orbitRadius.value
                val spotCenter = Offset(
                    x = center.x + cos(spot.angle.value) * radiusX,
                    y = center.y + sin(spot.angle.value) * radiusY
                )
                val minSize = size.minDimension * 0.10f
                val maxSize = size.minDimension * 0.20f
                val mainRadius = lerp(minSize,maxSize,spot.sizeProgress.value)
                val glowRadius = mainRadius * 4.5f
                val alpha = lerp(0.10f,0.32f,spot.alphaProgress.value)
                val color = discoColor(spot.colorProgress.value)

                drawCircle(
                    brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = alpha),
                                color.copy(alpha = alpha * 0.45f),
                                color.copy(alpha = alpha * 0.12f),
                                Color.Transparent
                            ),
                            center = spotCenter,
                            radius = glowRadius
                        ),
                    radius = glowRadius,
                    center = spotCenter
                )

                drawCircle(color = color.copy(alpha = alpha),radius = mainRadius,center = spotCenter)
            }
        }
    }

    private fun discoColor(progress: Float): Color {

        val colors = listOf(Color(0xFFFF4081),Color(0xFF00E5FF),Color(0xFFE040FB),
                            Color(0xFFFFD740),Color(0xFF00E676))
        val safeProgress = progress.coerceIn(0f,1f)
        val scaled = safeProgress * (colors.size - 1)
        val index = scaled.toInt().coerceIn(0,colors.size - 2)
        val nextIndex = index + 1
        val fraction = scaled - index
        return androidx.compose.ui.graphics.lerp(colors[index],colors[nextIndex], fraction)
    }
}

/* DISCO THEME */

val DiscoTheme =
    AppTheme(
        screenBackground = Color(0xFF160D2B),
        headerBackground = Color(0xFF2A1550),
        headerBorder = Color(0xFFFF4081),
        headerTitleText = Color(0xFFFF4081),
        headerSongText = Color(0xFF00E5FF),
        settingsIcon = Color(0xFFFFD740),
        sourceButtonColor = Color(0xFFFF4081),
        settingsButtonColor = Color(0xFF00E5FF),
        settingsBackground = Color(0xFF21103F),
        settingsText = Color(0xFFFFF176),
        settingsSecondaryText = Color(0xFFE040FB),
        listBackground = Color(0xFF33245A),
        listBorder = Color(0xFFE040FB),
        listText = Color(0xFFFFF176),
        allSongsColor = Color(0xFF00E5FF),
        folderColor = Color(0xFFFFD740),
        playlistColor = Color(0xFFFF4081),
        sourceIcon = Color(0xFFE040FB),
        randomActive = Color(0xFFFF4081),
        randomInactive = Color(0xFF765A9A),
        scrollTrack = Color(0xFF41296D),
        scrollThumb = Color(0xFF00E5FF),
        songBarTrack = Color(0xFF41296D),
        songBarProgress = Color(0xFFFF4081),
        songBarThumb = Color(0xFFFFD740),
        prevBg = Color(0xFF33245A),
        prevBorder = Color(0xFF00E5FF),
        prevIcon = Color(0xFFB2F7FF),
        playBg = Color(0xFFFF4081),
        playBorder = Color(0xFFFFD740),
        playIcon = Color.White,
        nextBg = Color(0xFF33245A),
        nextBorder = Color(0xFF00E5FF),
        nextIcon = Color(0xFFB2F7FF),
        songTimeText = Color(0xFF006EFF),
        activeSongColor = Color(0xFF008CFF),
        animation = DiscoAnimation
    )