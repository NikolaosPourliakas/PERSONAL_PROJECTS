package com.example.mymp3player.themes

import androidx.compose.ui.graphics.Color

fun AppTheme.interpolateTo(target: AppTheme, fraction: Float): AppTheme {
    val t = fraction.coerceIn(0f, 1f)

    fun mix(from: Color,to: Color): Color {
        return Color(
            red = from.red + (to.red - from.red) * t,
            green = from.green + (to.green - from.green) * t,
            blue = from.blue + (to.blue - from.blue) * t,
            alpha = from.alpha + (to.alpha - from.alpha) * t
        )
    }

    return copy(

        screenBackground = mix(screenBackground, target.screenBackground),
        headerBackground = mix(headerBackground, target.headerBackground),

        headerBorder = mix(headerBorder, target.headerBorder),
        headerTitleText = mix(headerTitleText, target.headerTitleText),
        headerSongText = mix(headerSongText, target.headerSongText),

        settingsIcon = mix(settingsIcon, target.settingsIcon),
        sourceButtonColor =mix(sourceButtonColor, target.sourceButtonColor),
        settingsButtonColor = mix(settingsButtonColor, target.settingsButtonColor),

        settingsBackground = mix(settingsBackground, target.settingsBackground),
        settingsText = mix(settingsText, target.settingsText),
        settingsSecondaryText = mix(settingsSecondaryText,target.settingsSecondaryText),

        listBackground = mix(listBackground, target.listBackground),
        listBorder = mix(listBorder, target.listBorder),
        listText = mix(listText, target.listText),
        activeSongColor = mix(activeSongColor, target.activeSongColor),

        allSongsColor = mix(allSongsColor, target.allSongsColor),
        folderColor = mix(folderColor, target.folderColor),
        playlistColor = mix(playlistColor, target.playlistColor),
        sourceIcon = mix(sourceIcon, target.sourceIcon),

        randomActive = mix(randomActive, target.randomActive),
        randomInactive = mix(randomInactive, target.randomInactive),

        scrollTrack =  mix(scrollTrack, target.scrollTrack),
        scrollThumb = mix(scrollThumb, target.scrollThumb),

        songBarTrack = mix(songBarTrack, target.songBarTrack),
        songBarProgress = mix(songBarProgress, target.songBarProgress),
        songBarThumb = mix(songBarThumb, target.songBarThumb),
        songTimeText = mix(songTimeText, target.songTimeText),

        prevBg = mix(prevBg, target.prevBg),
        prevBorder = mix(prevBorder, target.prevBorder),
        prevIcon = mix(prevIcon, target.prevIcon),

        playBg = mix(playBg, target.playBg),
        playBorder = mix(playBorder, target.playBorder),
        playIcon = mix(playIcon, target.playIcon),

        nextBg = mix(nextBg, target.nextBg),
        nextBorder = mix(nextBorder, target.nextBorder),
        nextIcon = mix(nextIcon, target.nextIcon)
    )
}