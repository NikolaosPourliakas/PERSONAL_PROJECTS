package com.example.mymp3player.themes.themes_setup

import androidx.compose.ui.graphics.Color
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.ThemeAnimation
import kotlin.random.Random

private enum class MonoComponent {
    HEADER,
    PREVIOUS_BUTTON,
    PLAY_BUTTON,
    NEXT_BUTTON,
    SONG_LIST,
    SONG_PROGRESS,
    LIST_SCROLLBAR
}

object MonoAnimation : ThemeAnimation {

    override val durationMillis: Int = 1500
    override val pauseMillis: Long = 500

    override fun nextTheme(baseTheme: AppTheme): AppTheme {

        val selectedComponents =
            MonoComponent.entries
                .filter {
                    Random.nextBoolean()
                }
                .toMutableSet()

        if (selectedComponents.isEmpty()) {
            selectedComponents += MonoComponent.entries.random()
        }

        var result = baseTheme

        /* HEADER */

        if (MonoComponent.HEADER in selectedComponents) {
            result =
                result.copy(
                    headerBackground = invertMono(baseTheme.headerBackground),
                    headerBorder = invertMono(baseTheme.headerBorder),
                    headerTitleText = invertMono(baseTheme.headerTitleText),
                    headerSongText = invertMono(baseTheme.headerSongText)
                )
        }

        /* PREVIOUS */
        if (MonoComponent.PREVIOUS_BUTTON in selectedComponents) {
            val bg = invertMono(baseTheme.prevBg)

            result = result.copy(
                prevBg = bg,
                prevBorder = invertMono(baseTheme.prevBorder),
                prevIcon = contrastMono(bg)
            )
        }

        /* PLAY / PAUSE */
        if (MonoComponent.PLAY_BUTTON in selectedComponents) {
            val bg = invertMono(baseTheme.playBg)

            result = result.copy(
                playBg = bg,
                playBorder = invertMono(baseTheme.playBorder),
                playIcon = contrastMono(bg)
            )
        }

        /* NEXT */
        if (MonoComponent.NEXT_BUTTON in selectedComponents) {
            val bg = invertMono(baseTheme.nextBg)

            result = result.copy(
                nextBg = bg,
                nextBorder = invertMono(baseTheme.nextBorder),
                nextIcon = contrastMono(bg)
            )
        }

        /* SONG LIST */

        if (MonoComponent.SONG_LIST in selectedComponents) {
            result =
                result.copy(
                    listBackground = invertMono(baseTheme.listBackground),
                    listBorder = invertMono(baseTheme.listBorder),
                    listText = invertMono(baseTheme.listText),
                    activeSongColor = invertMono(baseTheme.activeSongColor)
                )
        }

        /* SONG PROGRESS */

        if (MonoComponent.SONG_PROGRESS in selectedComponents) {
            val track = invertMono(baseTheme.songBarTrack)
            val progress = invertMono(baseTheme.songBarProgress)
            val thumb = invertMono(baseTheme.songBarThumb)

            result = result.copy(
                songBarTrack = track,
                songBarProgress = progress,
                songBarThumb = thumb,
                songTimeText = contrastMono(track)
            )
        }

        /* LIST SCROLLBAR */

        if (MonoComponent.LIST_SCROLLBAR in selectedComponents
        ) {
            result =
                result.copy(
                    scrollTrack = invertMono(baseTheme.screenBackground),
                    scrollThumb = invertMono(baseTheme.screenBackground)
                )
        }
        return result
    }
}

/* MONO THEME */

val MonoTheme =
    AppTheme(
        screenBackground = Color.Black,
        headerBackground = Color.Black,
        headerBorder = Color.White,
        headerTitleText = Color.White,
        headerSongText = Color.White,
        settingsIcon = Color.White,
        sourceButtonColor = Color.White,
        settingsButtonColor = Color.White,
        settingsBackground = Color.Black,
        settingsText = Color.White,
        settingsSecondaryText = Color.White,
        listBackground = Color.Black,
        listBorder = Color.White,
        listText = Color.White,
        activeSongColor = Color.White,
        allSongsColor = Color.White,
        folderColor = Color.White,
        playlistColor = Color.White,
        sourceIcon = Color.White,
        randomActive = Color.White,
        randomInactive = Color.White,
        scrollTrack = Color.Black,
        scrollThumb = Color.White,
        songBarTrack = Color.Black,
        songBarProgress = Color.White,
        songBarThumb = Color.White,
        songTimeText = Color.White,
        prevBg = Color.Black,
        prevBorder = Color.White,
        prevIcon = Color.White,
        playBg = Color.Black,
        playBorder = Color.White,
        playIcon = Color.White,
        nextBg = Color.Black,
        nextBorder = Color.White,
        nextIcon = Color.White,
        animation = MonoAnimation
    )

/* MONO COLOR INVERSION */

private fun invertMono(color: Color): Color {
    val luminance =
        color.red * 0.2126f +
                color.green * 0.7152f +
                color.blue * 0.0722f

    return if (luminance >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }
}

private fun contrastMono(background: Color): Color {
    val luminance =
        background.red * 0.2126f +
                background.green * 0.7152f +
                background.blue * 0.0722f

    return if (luminance >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }
}