package com.example.mymp3player.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp
import com.example.mymp3player.themes.AppTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberAnimatedTheme(theme: AppTheme,enabled: Boolean): AppTheme {

    val animation = theme.animation
    val animationProgress =remember {
            Animatable(0f)
        }
    var fromTheme by remember {
        mutableStateOf(theme)
    }
    var toTheme by remember {
        mutableStateOf(theme)
    }
    LaunchedEffect(theme,enabled,animation) {
        animationProgress.stop()
        fromTheme = theme
        toTheme = theme
        animationProgress.snapTo(0f)
        if (!enabled || animation == null) {
            return@LaunchedEffect
        }
        delay(250.milliseconds)
        while (true) {
            fromTheme = toTheme
            toTheme = animation.nextTheme(fromTheme)
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = animation.durationMillis,
                        easing = FastOutSlowInEasing
                    )
            )

            if (animation.pauseMillis > 0L) {
                delay(animation.pauseMillis.milliseconds)
            }
        }
    }

    if (!enabled || animation == null) {
        return theme
    }

    return interpolateTheme(from = fromTheme,to = toTheme, fraction = animationProgress.value)
}

private fun interpolateTheme(from: AppTheme,to: AppTheme, fraction: Float): AppTheme {

    val f = fraction.coerceIn(0f, 1f)

    return from.copy(
        screenBackground = lerp(from.screenBackground,to.screenBackground,f),
        headerBackground = lerp(from.headerBackground,to.headerBackground, f),
        headerBorder = lerp(from.headerBorder,to.headerBorder,f),
        headerTitleText = lerp(from.headerTitleText,to.headerTitleText,f),
        headerSongText = lerp(from.headerSongText,to.headerSongText,f),
        settingsIcon = lerp(from.settingsIcon,to.settingsIcon,f),
        sourceButtonColor = lerp(from.sourceButtonColor,to.sourceButtonColor,f),
        settingsButtonColor = lerp(from.settingsButtonColor,to.settingsButtonColor,f),
        settingsBackground = lerp(from.settingsBackground,to.settingsBackground,f),
        settingsText = lerp(from.settingsText,to.settingsText,f),
        settingsSecondaryText = lerp(from.settingsSecondaryText,to.settingsSecondaryText,f),
        listBackground = lerp(from.listBackground,to.listBackground,f),
        listBorder = lerp(from.listBorder,to.listBorder,f),
        listText = lerp(from.listText,to.listText,f),
        activeSongColor = lerp(from.activeSongColor,to.activeSongColor,f),
        allSongsColor = lerp(from.allSongsColor,to.allSongsColor,f),
        folderColor = lerp(from.folderColor,to.folderColor,f),
        playlistColor = lerp(from.playlistColor,to.playlistColor,f),
        sourceIcon = lerp(from.sourceIcon,to.sourceIcon,f),
        randomActive = lerp(from.randomActive,to.randomActive,f),
        randomInactive = lerp(from.randomInactive,to.randomInactive,f),
        scrollTrack = lerp(from.scrollTrack,to.scrollTrack,f),
        scrollThumb = lerp(from.scrollThumb,to.scrollThumb,f),
        songBarTrack = lerp(from.songBarTrack,to.songBarTrack,f),
        songBarProgress = lerp(from.songBarProgress,to.songBarProgress,f),
        songBarThumb = lerp(from.songBarThumb,to.songBarThumb,f),
        songTimeText = lerp(from.songTimeText,to.songTimeText,f),
        prevBg = lerp(from.prevBg,to.prevBg,f),
        prevBorder = lerp(from.prevBorder,to.prevBorder,f),
        prevIcon = lerp(from.prevIcon,to.prevIcon,f),
        playBg = lerp(from.playBg,to.playBg,f),
        playBorder = lerp(from.playBorder,to.playBorder,f),
        playIcon = lerp(from.playIcon,to.playIcon,f),
        nextBg = lerp(from.nextBg,to.nextBg,f),
        nextBorder = lerp(from.nextBorder,to.nextBorder,f),
        nextIcon = lerp(from.nextIcon,to.nextIcon,f),
        animation = from.animation
    )
}