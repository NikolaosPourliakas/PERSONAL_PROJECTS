package com.example.mymp3player.playlists

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymp3player.Playlist
import com.example.mymp3player.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach
import kotlin.collections.plus
import androidx.core.content.edit

@Suppress("TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNCHECKED_CAST")
class PlaylistController(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences(
            "mp3_player_playlists",
            Context.MODE_PRIVATE
        )

    private val _playlists = MutableStateFlow(loadPlaylists())
    val playlists: StateFlow<List<Playlist>> = _playlists
    private val _playlistGroups = MutableStateFlow(loadPlaylistGroups())

    val playlistGroups: StateFlow<List<PlaylistGroup>> = _playlistGroups

    fun createPlaylist(name: String) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        if (_playlists.value.any { it.name == cleanName }) return
        _playlists.value += Playlist(name = cleanName,songs = emptyList())
        savePlaylists()
    }

    fun deletePlaylist(name: String) {
        _playlists.value = _playlists.value.filterNot { it.name == name }
        savePlaylists()

        _playlistGroups.value = _playlistGroups.value.map { group ->
            group.copy(
                playlistNames = group.playlistNames.filterNot { it == name }
            )
        }
        savePlaylistGroups()
    }

    fun addSong(playlistName: String, song: Song) {
        _playlists.value = _playlists.value.map { playlist ->
                if (playlist.name == playlistName) {
                    if (playlist.songs.any {
                            it.assetPath == song.assetPath
                        }
                    ) {
                        playlist
                    } else {
                        playlist.copy(songs = playlist.songs + song)
                    }
                } else {
                    playlist
                }
            }
        savePlaylists()
    }

    fun removeSong(playlistName: String, song: Song) {
        _playlists.value = _playlists.value.map {
            playlist ->
                if (playlist.name == playlistName) {
                    playlist.copy(songs = playlist.songs.filterNot {
                            it.assetPath == song.assetPath
                        }
                    )
                } else {
                    playlist
                }
            }
        savePlaylists()
    }

    fun addSongs(playlistName: String, songsToAdd: List<Song>) {
        if (songsToAdd.isEmpty()) return

        val playlist = _playlists.value.find { it.name == playlistName }
            ?: return

        val existingPaths = playlist.songs
            .mapTo(HashSet()) { it.assetPath }

        val uniqueSongs = songsToAdd.filter { song ->
            existingPaths.add(song.assetPath)
        }

        if (uniqueSongs.isEmpty()) return

        _playlists.value = _playlists.value.map { current ->
            if (current.name == playlistName) {
                current.copy(songs = current.songs + uniqueSongs)
            } else {
                current
            }
        }

        savePlaylists()
    }

    fun removeSongs(playlistName: String, songsToRemove: List<Song>) {
        if (songsToRemove.isEmpty()) return

        val pathsToRemove = songsToRemove
            .mapTo(HashSet()) { it.assetPath }

        val playlist = _playlists.value.find { it.name == playlistName }
            ?: return

        val updatedSongs = playlist.songs.filterNot {
            it.assetPath in pathsToRemove
        }

        if (updatedSongs.size == playlist.songs.size) return

        _playlists.value = _playlists.value.map { current ->
            if (current.name == playlistName) {
                current.copy(songs = updatedSongs)
            } else {
                current
            }
        }

        savePlaylists()
    }

    fun createPlaylistGroup(name: String) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return

        if (_playlistGroups.value.any {
                it.name.equals(cleanName, ignoreCase = true)
            }
        ) return

        _playlistGroups.value =
            (_playlistGroups.value + PlaylistGroup(name = cleanName))
                .sortedBy { it.name.lowercase() }

        savePlaylistGroups()
    }

    fun deletePlaylistGroup(name: String) {
        _playlistGroups.value =
            _playlistGroups.value.filterNot { it.name == name }

        savePlaylistGroups()
    }

    fun addPlaylistToGroup(groupName: String, playlistName: String) {
        // Δεν επιτρέπουμε να ανατεθεί ανύπαρκτη playlist.
        if (_playlists.value.none { it.name == playlistName }) return

        _playlistGroups.value = _playlistGroups.value.map { group ->
            if (group.name == groupName &&
                group.playlistNames.none { it == playlistName }
            ) {
                group.copy(
                    playlistNames = (group.playlistNames + playlistName)
                        .sortedBy { it.lowercase() }
                )
            } else {
                group
            }
        }

        savePlaylistGroups()
    }

    fun removePlaylistFromGroup(groupName: String, playlistName: String) {
        _playlistGroups.value = _playlistGroups.value.map { group ->
            if (group.name == groupName) {
                group.copy(
                    playlistNames = group.playlistNames.filterNot {
                        it == playlistName
                    }
                )
            } else {
                group
            }
        }

        savePlaylistGroups()
    }

    private fun savePlaylistGroups() {
        val array = JSONArray()

        _playlistGroups.value.forEach { group ->
            val groupObject = JSONObject()
            groupObject.put("name", group.name)

            val playlistsArray = JSONArray()
            group.playlistNames.forEach { playlistName ->
                playlistsArray.put(playlistName)
            }

            groupObject.put("playlists", playlistsArray)
            array.put(groupObject)
        }

        preferences.edit {
            putString("playlist_groups", array.toString())
        }
    }

    private fun loadPlaylistGroups(): List<PlaylistGroup> {
        val saved = preferences.getString("playlist_groups", null)
            ?: return emptyList()

        return try {
            val array = JSONArray(saved)
            val groups = mutableListOf<PlaylistGroup>()

            for (i in 0 until array.length()) {
                val groupObject = array.getJSONObject(i)
                val namesArray =
                    groupObject.optJSONArray("playlists") ?: JSONArray()

                val names = mutableListOf<String>()

                for (j in 0 until namesArray.length()) {
                    val name = namesArray.optString(j)
                    if (name.isNotBlank()) names.add(name)
                }

                groups.add(
                    PlaylistGroup(
                        name = groupObject.getString("name"),
                        playlistNames = names.distinct()
                            .sortedBy { it.lowercase() }
                    )
                )
            }

            groups.sortedBy { it.name.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun savePlaylists() {
        val array = JSONArray()
        _playlists.value.forEach { playlist -> val playlistObject = JSONObject()
            playlistObject.put("name",playlist.name)
            val songsArray = JSONArray()
            playlist.songs.forEach { song -> val songObject = JSONObject()
                songObject.put("title",song.title)
                songObject.put("assetPath",song.assetPath)
                songObject.put("folderName",song.folderName)
                songsArray.put(songObject)
            }
            playlistObject.put("songs",songsArray)
            array.put(playlistObject)
        }
        preferences.edit { putString("playlists", array.toString()) }
    }

    private fun loadPlaylists(): List<Playlist> {
        val saved = preferences.getString("playlists",null) ?: return emptyList()

        return try {
            val array = JSONArray(saved)
            val result = mutableListOf<Playlist>()

            for (i in 0 until array.length()) {
                val playlistObject = array.getJSONObject(i)
                val name = playlistObject.getString("name")
                val songsArray = playlistObject.optJSONArray("songs") ?: JSONArray()
                val songs = mutableListOf<Song>()

                for (j in 0 until songsArray.length()) {
                    val songObject = songsArray.getJSONObject(j)
                    songs.add(
                        Song(
                            title = songObject.getString("title"),
                            assetPath = songObject.getString("assetPath"),
                            folderName =
                                if (songObject.isNull("folderName")) {
                                    null
                                } else {
                                    songObject.optString("folderName",null)
                                }
                        )
                    )
                }
                result.add(Playlist(name = name,songs = songs))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object :
                ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlaylistController(application) as T
                }
            }
        }
    }
}