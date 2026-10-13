package com.example.mymp3player.audio

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.mymp3player.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class AudioController(context: Context) {

    private val appContext = context.applicationContext
    private var controller: MediaController? = null
    private var songs: List<Song> = emptyList()
    private var cachedMediaItems: List<MediaItem> = emptyList()
    private var currentIndex = 0
    private var currentPlayingSongUri: String? = null
    private var currentMediaAssetPath: String? = null
    private val playbackHistory = ArrayDeque<String>()
    private var pendingIndex = 0
    private var pendingPlay = false
    private var pendingRandom = false

    var onPlayStateChanged: ((Boolean) -> Unit)? = null
    var onSongChanged: ((Int) -> Unit)? = null
    var onPositionChanged: ((Long, Long) -> Unit)? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()

        scope.launch {
            try {
                val mediaController = withContext(Dispatchers.IO) {
                    controllerFuture.get()
                }

                if (!isActive) {
                    mediaController.release()
                    return@launch
                }

                controller = mediaController
                mediaController.shuffleModeEnabled = pendingRandom

                mediaController.addListener(
                    object : Player.Listener {

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            onPlayStateChanged?.invoke(isPlaying)
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                next()
                            }
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            val assetPath = mediaItem?.mediaId
                                ?.takeIf { id -> songs.any { it.assetPath == id } }
                                ?: return

                            val index = songs.indexOfFirst { it.assetPath == assetPath }
                            if (index < 0) return

                            currentIndex = index
                            currentPlayingSongUri = assetPath
                            currentMediaAssetPath = assetPath

                            onSongChanged?.invoke(currentIndex)
                        }
                    }
                )

                if (pendingPlay) {
                    val index = pendingIndex
                    pendingPlay = false
                    playIndex(index)
                }
            } catch (_: Exception) {
                pendingPlay = false
            }
        }

        scope.launch {
            while (isActive) {
                val currentController = controller

                if (currentController != null) {
                    val duration = currentController.duration.coerceAtLeast(0L)
                    onPositionChanged?.invoke(currentController.currentPosition, duration)
                }

                delay(500.milliseconds)
            }
        }
    }

    fun setPlaylist(songs: List<Song>) {
        val newSongs = songs.distinctBy { it.assetPath }
        val oldPlayingUri = currentPlayingSongUri
        val currentController = controller

        this.songs = newSongs

        cachedMediaItems = newSongs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.assetPath)
                .setUri(createSongUri(song))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist("MyMP3Player")
                        .build()
                )
                .build()
        }

        if (newSongs.isEmpty()) {
            currentIndex = 0
            currentPlayingSongUri = null
            currentMediaAssetPath = null
            playbackHistory.clear()
            return
        }

        val newIndex = newSongs.indexOfFirst {
            it.assetPath == oldPlayingUri
        }

        if (newIndex >= 0) {
            currentIndex = newIndex
            currentPlayingSongUri = newSongs[newIndex].assetPath
            currentMediaAssetPath = newSongs[newIndex].assetPath

        } else {
            currentIndex = 0
            currentPlayingSongUri = null
            currentMediaAssetPath = null
        }
    }

    fun restoreSong(songUri: String?, autoplay: Boolean) {
        if (songUri.isNullOrBlank()) {
            return
        }

        val index = songs.indexOfFirst { it.assetPath == songUri }

        if (index < 0) {
            return
        }

        currentIndex = index
        currentPlayingSongUri = songs[index].assetPath
        currentMediaAssetPath = songs[index].assetPath

        if (autoplay) {
            if (controller == null) {
                pendingIndex = index
                pendingPlay = true
            } else {
                playIndex(index)
            }
        }
    }

    fun getCurrentSongUri(): String? {
        return currentPlayingSongUri
    }

    fun play() {
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun seekTo(index: Int) {
        if (index !in songs.indices) {
            return
        }

        if (controller == null) {
            pendingIndex = index
            pendingPlay = true
            return
        }

        playIndex(index)
    }

    private fun playIndex(index: Int) {
        val currentController = controller ?: return

        if (index !in songs.indices) {
            return
        }

        currentIndex = index
        currentPlayingSongUri = songs[index].assetPath

        currentController.setMediaItems(cachedMediaItems, index, 0L)
        currentController.prepare()
        currentController.play()

        onSongChanged?.invoke(currentIndex)
    }

    private fun createSongUri(song: Song) =
        if (song.assetPath.startsWith("content://")) {
            song.assetPath.toUri()
        } else {
            "asset:///${song.assetPath}".toUri()
        }

    fun next() {
        if (songs.isEmpty()) {
            return
        }

        val currentUri = currentPlayingSongUri

        if (!currentUri.isNullOrBlank()) {
            playbackHistory.addLast(currentUri)
        }

        val currentListIndex =
            if (!currentUri.isNullOrBlank()) {
                songs.indexOfFirst { it.assetPath == currentUri }
            } else {
                -1
            }

        val baseIndex =
            if (currentListIndex >= 0) {
                currentListIndex
            } else {
                -1
            }

        val nextIndex =
            if (pendingRandom) {
                randomIndex(baseIndex)
            } else {
                if (baseIndex + 1 in songs.indices) {
                    baseIndex + 1
                } else {
                    0
                }
            }

        playIndex(nextIndex)
    }

    fun previous() {
        if (songs.isEmpty()) {
            return
        }

        while (playbackHistory.isNotEmpty()) {
            val previousUri = playbackHistory.removeLast()
            val previousIndex = songs.indexOfFirst { it.assetPath == previousUri }

            if (previousIndex >= 0) {
                playIndex(previousIndex)
                return
            }
        }

        val currentListIndex =
            if (!currentPlayingSongUri.isNullOrBlank()) {
                songs.indexOfFirst { it.assetPath == currentPlayingSongUri }
            } else {
                -1
            }

        if (currentListIndex >= 0 && !pendingRandom) {
            val previousIndex =
                if (currentListIndex - 1 >= 0) {
                    currentListIndex - 1
                } else {
                    songs.lastIndex
                }

            playIndex(previousIndex)
        }
    }

    private fun randomIndex(baseIndex: Int): Int {
        if (songs.size <= 1) {
            return 0
        }

        var index = Random.nextInt(songs.size)

        if (baseIndex < 0) {
            return index
        }

        while (index == baseIndex) {
            index = Random.nextInt(songs.size)
        }

        return index
    }


    fun setRandom(enabled: Boolean) {
        pendingRandom = enabled
        controller?.shuffleModeEnabled = enabled
    }
    
    fun seekToPosition(position: Long) {
        controller?.seekTo(position.coerceAtLeast(0L))
    }

    fun release() {
        scope.cancel()
        controller?.release()
        controller = null
    }
}
