package com.example.mymp3player.themes.themes_setup

import androidx.compose.ui.graphics.Color
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.ThemeAnimation
import kotlin.random.Random

/* ORIGINAL ANIMATION */

private object OriginalAnimation : ThemeAnimation {

    override val durationMillis = 3200
    override val pauseMillis = 0L

    override fun nextTheme(baseTheme: AppTheme): AppTheme {

        return baseTheme.copy(
            screenBackground = randomColor(),
            headerBackground = randomColor(),
            headerBorder = randomColor(),
            headerTitleText = randomColor(),
            headerSongText = randomColor(),
            settingsIcon = randomColor(),
            sourceButtonColor = randomColor(),
            settingsButtonColor = randomColor(),
            settingsBackground = randomColor(),
            settingsText = randomColor(),
            settingsSecondaryText = randomColor(),
            listBackground = randomColor(),
            listBorder = randomColor(),
            listText = randomColor(),
            activeSongColor = randomColor(),
            allSongsColor = randomColor(),
            folderColor = randomColor(),
            playlistColor = randomColor(),
            sourceIcon = randomColor(),
            randomActive = randomColor(),
            randomInactive = randomColor(),
            scrollTrack = randomColor(),
            scrollThumb = randomColor(),
            songBarTrack = randomColor(),
            songBarProgress = randomColor(),
            songBarThumb = randomColor(),
            songTimeText = randomColor(),
            prevBg = randomColor(),
            prevBorder = randomColor(),
            prevIcon = randomColor(),
            playBg = randomColor(),
            playBorder = randomColor(),
            playIcon =  randomColor(),
            nextBg = randomColor(),
            nextBorder = randomColor(),
            nextIcon = randomColor()
        )
    }
}

private fun randomColor(): Color {

    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f
    )
}

/* ORIGINAL THEME */

val OriginalTheme = AppTheme(
    screenBackground = Color(0xFFFFE6D5),
    headerBackground = Color(0xFFFFA726),
    headerBorder = Color(0xFFE65100),
    headerTitleText = Color(0xFF0057FF),
    headerSongText = Color(0xFF0057FF),
    settingsIcon = Color(0xFF7B1FA2),
    sourceButtonColor = Color(0xFF00897B),
    settingsButtonColor = Color(0xFF0057FF),
    settingsBackground = Color(0xFFFFF3E0),
    settingsText = Color(0xFF3E2723),
    settingsSecondaryText = Color(0xFF6D4C41),
    listBackground = Color.White,
    listBorder = Color(0xFFE65100),
    listText = Color(0xFF263238),
    allSongsColor = Color(0xFF006EFF),
    folderColor = Color(0xFF00897B),
    playlistColor = Color(0xFFE91E63),
    sourceIcon = Color(0xFF7B1FA2),
    randomActive = Color(0xFFE91E63),
    randomInactive = Color(0xFF9E9E9E),
    scrollTrack = Color(0xFFFFCC80),
    scrollThumb = Color(0xFFE65100),
    songBarTrack = Color(0xFFFFCC80),
    songBarProgress = Color(0xFF64D8FF),
    songBarThumb = Color(0xFF38BDF8),
    songTimeText = Color(0xFF006EFF),
    prevBg = Color(0xFFFF9800),
    prevBorder = Color(0xFFE65100),
    prevIcon = Color(0xFFFF1800),
    playBg = Color(0xFFFF9800),
    playBorder = Color(0xFFE65100),
    playIcon = Color(0xFF7B1FA2),
    nextBg = Color(0xFFFF9800),
    nextBorder = Color(0xFFE65100),
    nextIcon = Color(0xFFFF1800),
    activeSongColor = Color(0xFF7B1FA2),
    animation = OriginalAnimation
)