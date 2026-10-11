package com.example.mymp3player.themes

import androidx.compose.ui.graphics.Color
import com.example.mymp3player.themes.themes_setup.CyberTheme
import com.example.mymp3player.themes.themes_setup.DiscoTheme
import com.example.mymp3player.themes.themes_setup.MonoTheme
import com.example.mymp3player.themes.themes_setup.OriginalTheme
import com.example.mymp3player.themes.themes_setup.PinballTheme
import com.example.mymp3player.themes.themes_setup.ConcertTheme

/* -----THEME ANIMATION ----- */

interface ThemeAnimation {

    val durationMillis: Int
    val pauseMillis: Long
    fun nextTheme(baseTheme: AppTheme): AppTheme
}

/* ----- APP THEME ----- */

data class AppTheme(
    val screenBackground: Color,
    val headerBackground: Color,
    val headerBorder: Color,
    val headerTitleText: Color,
    val headerSongText: Color,

    val settingsIcon: Color,
    val sourceButtonColor: Color,
    val settingsButtonColor: Color,

    val settingsBackground: Color,
    val settingsText: Color,
    val settingsSecondaryText: Color,

    val listBackground: Color,
    val listBorder: Color,
    val listText: Color,
    val activeSongColor: Color,

    val allSongsColor: Color,
    val folderColor: Color,
    val playlistColor: Color,
    val sourceIcon: Color,

    val randomActive: Color,
    val randomInactive: Color,

    val scrollTrack: Color,
    val scrollThumb: Color,

    val songBarTrack: Color,
    val songBarProgress: Color,
    val songBarThumb: Color,
    val songTimeText: Color,

    val prevBg: Color,
    val prevBorder: Color,
    val prevIcon: Color,

    val playBg: Color,
    val playBorder: Color,
    val playIcon: Color,

    val nextBg: Color,
    val nextBorder: Color,
    val nextIcon: Color,

    val animation: ThemeAnimation? = null
)

data class ThemeItem(var name: String,val theme: AppTheme)

val DefaultTheme: AppTheme
    get() = OriginalTheme

// ALL THEMES

val AllInitialThemes = listOf(
    ThemeItem("Original", OriginalTheme),
    ThemeItem("Mono", MonoTheme),
    ThemeItem("Pinball", PinballTheme),
    ThemeItem("Disco", DiscoTheme),
    ThemeItem("Cyber", CyberTheme),
    ThemeItem("Concert", ConcertTheme)
)