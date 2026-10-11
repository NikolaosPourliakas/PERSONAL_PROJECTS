package com.example.mymp3player.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mymp3player.themes.AppTheme

// COLORS

@Composable
fun ColorSettingsPage(theme: AppTheme, onEditColor: (String) -> Unit) {

    val settingsBackground = Color(0xFFF5F5F5)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(settingsBackground)
    ) {
        item {
            ColorRow("screenBackground", theme.screenBackground, onEditColor)
        }
        item {
            ColorRow("headerBackground", theme.headerBackground, onEditColor)
        }
        item {
            ColorRow("headerBorder", theme.headerBorder, onEditColor)
        }
        item {
            ColorRow("headerTitleText", theme.headerTitleText, onEditColor)
        }
        item {
            ColorRow("headerSongText", theme.headerSongText, onEditColor)
        }
        item {
            ColorRow("settingsIcon", theme.settingsIcon, onEditColor)
        }
        item {
            ColorRow("settingsBackground", theme.settingsBackground, onEditColor)
        }
        item {
            ColorRow("settingsText", theme.settingsText, onEditColor)
        }
        item {
            ColorRow("settingsSecondaryText", theme.settingsSecondaryText, onEditColor)
        }
        item {
            ColorRow("listBackground", theme.listBackground, onEditColor)
        }
        item {
            ColorRow("listBorder", theme.listBorder, onEditColor)
        }
        item {
            ColorRow("listText", theme.listText, onEditColor)
        }
        item {
            ColorRow("allSongsColor", theme.allSongsColor, onEditColor)
        }
        item {
            ColorRow("folderColor", theme.folderColor, onEditColor)
        }
        item {
            ColorRow("playlistColor", theme.playlistColor, onEditColor)
        }
        item {
            ColorRow("sourceIcon", theme.sourceIcon, onEditColor)
        }
        item {
            ColorRow("randomActive", theme.randomActive, onEditColor)
        }
        item {
            ColorRow("randomInactive", theme.randomInactive, onEditColor)
        }
        item {
            ColorRow("scrollTrack", theme.scrollTrack, onEditColor)
        }
        item {
            ColorRow("scrollThumb", theme.scrollThumb, onEditColor)
        }
        item {
            ColorRow("songBarTrack", theme.songBarTrack, onEditColor)
        }
        item {
            ColorRow("songBarProgress", theme.songBarProgress, onEditColor)
        }
        item {
            ColorRow("songBarThumb", theme.songBarThumb, onEditColor)
        }
        item {
            ColorRow("prevBg", theme.prevBg, onEditColor)
        }
        item {
            ColorRow("prevBorder", theme.prevBorder, onEditColor)
        }
        item {
            ColorRow("prevIcon", theme.prevIcon, onEditColor)
        }
        item {
            ColorRow("playBg", theme.playBg, onEditColor)
        }
        item {
            ColorRow("playBorder", theme.playBorder, onEditColor)
        }
        item {
            ColorRow("playIcon", theme.playIcon, onEditColor)
        }
        item {
            ColorRow("nextBg", theme.nextBg, onEditColor)
        }
        item {
            ColorRow("nextBorder", theme.nextBorder, onEditColor)
        }
        item {
            ColorRow("nextIcon", theme.nextIcon, onEditColor)
        }
    }
}