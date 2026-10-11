package com.example.mymp3player

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.DefaultTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.mymp3player.themes.AllInitialThemes
import org.json.JSONArray
import org.json.JSONObject

data class PlayerState(
    val songs: List<Song> = emptyList(),
    val allSongs: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isRandom: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val theme: AppTheme = DefaultTheme,
    val playInBackground: Boolean = false,
    val pauseOnHeadphoneDisconnect: Boolean = true,
    val animateSongTitle: Boolean = true,
    val keepScreenOn: Boolean = false,
    val themeEffectTick: Long = 0L,
    val themeAnimationsEnabled: Set<String> = emptySet(),
    val isLoadingSongs: Boolean = false,
    val loadingSongsCount: Int = 0
)

class UIController(application: Application) : AndroidViewModel(application) {

    private val preferences =
        application.getSharedPreferences(
            "mp3_player_preferences",
            Context.MODE_PRIVATE
        )

    private companion object {
        const val KEY_THEME_LIGHTING_PREFIX = "theme_lighting_"
        const val KEY_ANIMATE_SONG_TITLE = "animate_song_title"
        const val KEY_RANDOM_ENABLED = "random_enabled"
        const val KEY_THEME_NAME = "selected_theme_name"
        const val KEY_CACHED_SONGS = "cached_songs"
        const val KEY_LAST_SOURCE = "last_source"
        const val KEY_SELECTED_SONG_PATHS = "selected_song_paths"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    }

    private fun loadThemeLighting(): Set<String> {
        return preferences.all
            .filterKeys {
                it.startsWith(KEY_THEME_LIGHTING_PREFIX)
            }
            .filterValues {
                it == true
            }
            .keys
            .map {
                it.removePrefix(KEY_THEME_LIGHTING_PREFIX)
            }
            .toSet()
    }

    private fun loadSavedTheme(): AppTheme {
        val savedName = preferences.getString(KEY_THEME_NAME, "Original")

        return AllInitialThemes.firstOrNull { it.name == savedName }?.theme
            ?: DefaultTheme
    }

    private val _state = MutableStateFlow(
        PlayerState(
            isRandom = preferences.getBoolean(KEY_RANDOM_ENABLED, false),
            theme = loadSavedTheme(),
            themeAnimationsEnabled = loadThemeLighting(),
            animateSongTitle = preferences.getBoolean(
                KEY_ANIMATE_SONG_TITLE,
                true
            ),
            keepScreenOn = preferences.getBoolean(
                KEY_KEEP_SCREEN_ON,
                false
            ),
            playInBackground = preferences.getBoolean(
                "play_in_background",
                false
            ),
            pauseOnHeadphoneDisconnect = preferences.getBoolean(
                "pause_on_headphone_disconnect",
                true
            )
        )
    )


    val state: StateFlow<PlayerState> = _state

    // SONGS

    fun setSongs(list: List<Song>) {
        val uniqueSongs = list.distinctBy {
            it.assetPath
        }

        _state.value = _state.value.copy(
            songs = uniqueSongs,
            allSongs = uniqueSongs,
            currentIndex = 0,
            isLoadingSongs = false,
            loadingSongsCount = uniqueSongs.size
        )
    }

    fun saveCachedSongs(list: List<Song>) {
        val jsonArray = JSONArray()

        list.distinctBy { it.assetPath }.forEach { song ->
            val item = JSONObject()
            item.put("title", song.title)
            item.put("assetPath", song.assetPath)
            item.put("folderName", song.folderName)
            jsonArray.put(item)
        }

        preferences.edit {
            putString(KEY_CACHED_SONGS, jsonArray.toString())
        }
    }

    fun loadCachedSongs(): List<Song> {
        val json = preferences.getString(KEY_CACHED_SONGS, null)
            ?: return emptyList()

        return try {
            val jsonArray = JSONArray(json)
            buildList {
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)

                    add(
                        Song(
                            title = item.getString("title"),
                            assetPath = item.getString("assetPath"),
                            folderName = item.optString("folderName")
                                .takeIf { it.isNotBlank() && it != "null" }
                        )
                    )
                }
            }.distinctBy { it.assetPath }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveLastSource(source: String) {
        preferences.edit {
            putString(KEY_LAST_SOURCE, source)
        }
    }

    fun loadLastSource(): String {
        return preferences.getString(KEY_LAST_SOURCE, "All Songs")
            ?: "All Songs"
    }

    fun saveSelectedSongPaths(paths: Set<String>) {
        val jsonArray = JSONArray()
        paths.forEach { path ->
            jsonArray.put(path)
        }

        preferences.edit {
            putString(KEY_SELECTED_SONG_PATHS, jsonArray.toString())
        }
    }

    fun loadSelectedSongPaths(): Set<String> {
        val json = preferences.getString(KEY_SELECTED_SONG_PATHS, null)
            ?: return emptySet()

        return try {
            val jsonArray = JSONArray(json)
            buildSet {
                for (i in 0 until jsonArray.length()) {
                    add(jsonArray.getString(i))
                }
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun setAllSongsPreservingSelection(list: List<Song>) {
        val uniqueSongs = list.distinctBy { it.assetPath }
        val lastSource = loadLastSource()
        val selectedPaths = loadSelectedSongPaths()

        val displayedSongs = if (
            lastSource == "All Songs" || selectedPaths.isEmpty()
        ) {
            uniqueSongs
        } else {
            val matchingSongs = uniqueSongs.filter {
                it.assetPath in selectedPaths
            }

            // Αν δεν βρεθούν ακόμη αντιστοιχίες, κράτησε όλα τα τραγούδια
            // αντί να εμφανίσεις προσωρινά άδεια λίστα.
            matchingSongs.ifEmpty { uniqueSongs }
        }

        val oldState = _state.value
        val oldCurrentPath = oldState.songs
            .getOrNull(oldState.currentIndex)
            ?.assetPath

        val newIndex = displayedSongs.indexOfFirst {
            it.assetPath == oldCurrentPath
        }.let { index ->
            if (index >= 0) index else 0
        }

        _state.value = oldState.copy(
            songs = displayedSongs,
            allSongs = uniqueSongs,
            currentIndex = newIndex,
            isLoadingSongs = false,
            loadingSongsCount = uniqueSongs.size
        )
    }

    fun appendSongs(batch: List<Song>) {
        if (batch.isEmpty()) return

        val oldState = _state.value
        val existingPaths = oldState.allSongs
            .mapTo(mutableSetOf()) { it.assetPath }

        val newSongs = batch.filter { it.assetPath !in existingPaths }

        if (newSongs.isEmpty()) return

        val updatedAllSongs = oldState.allSongs + newSongs

        val showingAllSongs = oldState.songs.size == oldState.allSongs.size &&
                oldState.songs.map { it.assetPath }.toSet() == existingPaths

        _state.value = oldState.copy(
            allSongs = updatedAllSongs,
            songs = if (showingAllSongs) {
                oldState.songs + newSongs
            } else {
                oldState.songs
            },
            loadingSongsCount = updatedAllSongs.size
        )
    }

    // PLAYING

    fun setPlaying(value: Boolean) {
        _state.value = _state.value.copy(isPlaying = value)
    }

    // RANDOM

    fun setRandom(value: Boolean) {
        preferences.edit {
            putBoolean(KEY_RANDOM_ENABLED, value)
        }
        _state.value = _state.value.copy(isRandom = value)
    }

    // INDEX

    fun setIndex(index: Int) {
        if (index < 0 || index >= _state.value.songs.size) {
            return
        }

        _state.value = _state.value.copy(currentIndex = index)
    }

    // POSITION

    fun setPosition(position: Long, duration: Long) {
        _state.value = _state.value.copy(
            currentPosition = position,
            duration = duration
        )
    }

    // THEME

    fun setTheme(theme: AppTheme) {
        val themeName = AllInitialThemes
            .firstOrNull { it.theme == theme }
            ?.name

        if (themeName != null) {
            preferences.edit {
                putString("selected_theme_name", themeName)
            }
        }

        _state.value = _state.value.copy(theme = theme)
    }

    // THEME LIGHTING

    fun setThemeAnimationEnabled(
        themeName: String,
        enabled: Boolean
    ) {
        val current = _state.value.themeAnimationsEnabled.toMutableSet()

        if (enabled) {
            current.add(themeName)
        } else {
            current.remove(themeName)
        }

        preferences.edit {
            putBoolean(
                KEY_THEME_LIGHTING_PREFIX + themeName,
                enabled
            )
        }

        _state.value = _state.value.copy(
            themeAnimationsEnabled = current
        )
    }

    // SONG TITLE ANIMATION

    fun setAnimateSongTitle(enabled: Boolean) {
        preferences.edit {
            putBoolean(KEY_ANIMATE_SONG_TITLE, enabled)
        }

        _state.value = _state.value.copy(
            animateSongTitle = enabled
        )
    }

    // KEEP SCREEN ON

    fun setKeepScreenOn(enabled: Boolean) {
        preferences.edit {
            putBoolean(KEY_KEEP_SCREEN_ON, enabled)
        }

        _state.value = _state.value.copy(
            keepScreenOn = enabled
        )
    }

    // BACKGROUND PLAY

    fun setPlayInBackground(value: Boolean) {
        preferences.edit {
            putBoolean("play_in_background", value)
        }

        _state.value = _state.value.copy(
            playInBackground = value
        )
    }

    fun setPauseOnHeadphoneDisconnect(value: Boolean) {
        preferences.edit {
            putBoolean("pause_on_headphone_disconnect", value)
        }

        _state.value = _state.value.copy(
            pauseOnHeadphoneDisconnect = value
        )
    }

    // DISPLAYED SONGS

    fun setDisplayedSongs(list: List<Song>) {
        _state.value = _state.value.copy(
            songs = list,
            currentIndex = 0
        )
    }

    // LOADING

    fun setLoadingSongs(value: Boolean) {
        _state.value = _state.value.copy(
            isLoadingSongs = value,
            loadingSongsCount = if (value) {
                0
            } else {
                _state.value.loadingSongsCount
            }
        )
    }

    fun setLoadingSongsCount(count: Int) {
        _state.value = _state.value.copy(
            loadingSongsCount = count
        )
    }
}