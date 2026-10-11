package com.example.mymp3player.audio

import com.example.mymp3player.Song
import com.example.mymp3player.UIController

class ButtonsController(
    private val audio: AudioController,
    private val ui: UIController
) {

    fun playPause(isPlaying: Boolean) {
        if (isPlaying) {
            audio.pause()
        } else {
            audio.play()
        }
    }

    fun next() {
        audio.next()
    }

    fun previous() {
        audio.previous()
    }

    fun selectSong(index: Int) {
        audio.seekTo(index)
    }

    fun seekToPosition(position: Long) {
        audio.seekToPosition(position)
    }

    fun setRandom(enabled: Boolean) {
        audio.setRandom(enabled)
        ui.setRandom(enabled)
    }

    fun setPlaylist(songs: List<Song>) {
        audio.setPlaylist(songs)
    }

    fun getCurrentSongUri(): String? {
        return audio.getCurrentSongUri()
    }
}