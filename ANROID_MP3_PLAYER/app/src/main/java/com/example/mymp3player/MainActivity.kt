package com.example.mymp3player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.mymp3player.audio.AudioController
import com.example.mymp3player.audio.ButtonsController
import com.example.mymp3player.player.PlayerScreen
import com.example.mymp3player.playlists.PlaylistController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import androidx.core.net.toUri
import androidx.core.content.edit
import android.view.WindowManager

class MainActivity : ComponentActivity() {

    private lateinit var audioController: AudioController
    private lateinit var buttons: ButtonsController
    private val uiController: UIController by viewModels()
    private val playlistController: PlaylistController by viewModels {
        PlaylistController.factory(application)
    }
    private var scanJob: Job? = null

    // PREFERENCES

    private val preferences by lazy {
        getSharedPreferences("mp3_player_preferences",MODE_PRIVATE)
    }
    private companion object {
        const val KEY_LAST_FOLDER_URI = "last_folder_uri"
        const val KEY_LAST_SONG_URI = "last_song_uri"
        const val TAG = "MP3_SCANNER"
    }

    // AUDIO EXTENSIONS

    private val audioExtensions =
        setOf("mp3","m4a","aac","wav","flac","ogg","opus","wma","3gp","mka","amr","ape","alac")

    // DOCUMENT QUERY

    private val documentProjection =
        arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

    // FOLDER PICKER

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            uri: Uri? ->
            if (uri == null) {
                return@registerForActivityResult
            }
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.w(TAG,"Could not persist folder permission",e
                )
            }
            saveLastFolder(uri)
            loadSongsFromFolder(uri)
        }

    // CREATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioController = AudioController(this)
        buttons = ButtonsController(audio = audioController,ui = uiController)

        // AUDIO CALLBACKS

        audioController.onPlayStateChanged =
            { playing ->
                uiController.setPlaying(playing)
            }

        audioController.onSongChanged =
            { index ->
                uiController.setIndex(index)
                val song = uiController.state.value.songs.getOrNull(index)
                if (song != null) {
                    saveLastSong(song.assetPath)
                }
            }

        audioController.onPositionChanged =
            { position, duration ->
                uiController.setPosition(position,duration)
            }

        // UI

        setContent {

            val state by
            uiController.state.collectAsState()

            androidx.compose.runtime.SideEffect {
                if (state.keepScreenOn) {
                    window.addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                } else {
                    window.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                }
            }

            val currentSong = state.songs.getOrNull(state.currentIndex)

            PlayerScreen(
                state = state,
                currentSong = currentSong,
                buttons = buttons,
                uiController = uiController,
                playlistController = playlistController,

                onMusicFolderClick = {
                    openMusicFolderPicker()
                }
            )
        }
        loadInitialSongs()
    }

    // INITIAL LOAD

    private fun loadInitialSongs() {
        val cachedSongs = uiController.loadCachedSongs()

        if (cachedSongs.isNotEmpty()) {
            uiController.setAllSongsPreservingSelection(cachedSongs)

            audioController.setPlaylist(uiController.state.value.songs)

            audioController.restoreSong(
                songUri = getLastSong(),
                autoplay = false
            )
        }

        val savedFolder = getLastFolder()

        if (savedFolder != null) {
            loadSongsFromFolder(
                Uri.parse(savedFolder),
                keepCachedSongs = cachedSongs.isNotEmpty()
            )
        } else if (cachedSongs.isEmpty()) {
            loadSongsFromAssets()
        }
    }

    // LAST FOLDER
    private fun saveLastFolder(uri: Uri) {
        preferences
            .edit {
                putString(KEY_LAST_FOLDER_URI, uri.toString())
            }
    }

    private fun getLastFolder(): String? {
        return preferences.getString(KEY_LAST_FOLDER_URI,null)
    }

    // LAST SONG

    private fun saveLastSong(songUri: String) {
        preferences
            .edit {
                putString(KEY_LAST_SONG_URI, songUri)
            }
    }

    private fun getLastSong(): String? {
        return preferences
            .getString(KEY_LAST_SONG_URI,null)
    }

    // FOLDER PICKER

    private fun openMusicFolderPicker() {
        folderPickerLauncher.launch(null)
    }

    // ASSETS

    private fun loadSongsFromAssets() {

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val songs = ArrayList<Song>()
                val folders =  assets.list("") ?: emptyArray()

                for (folder in folders) {
                    currentCoroutineContext().ensureActive()

                    val files = assets.list(folder) ?: continue

                    for (file in files) {
                        if (file.endsWith(".mp3",ignoreCase = true)) {
                            songs.add(
                                Song(
                                    title = file.substringBeforeLast("."),
                                    assetPath = "$folder/$file",
                                    folderName = folder
                                )
                            )
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    uiController.setAllSongsPreservingSelection(songs)
                    uiController.saveCachedSongs(songs)
                    audioController.setPlaylist(uiController.state.value.songs)
                    audioController.restoreSong(
                        songUri = getLastSong(),
                        autoplay = false
                    )
                }
                Log.d(TAG,"Assets: ${songs.size} songs")

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(TAG,"Could not load assets",e)
                withContext(Dispatchers.Main) {
                    uiController.setLoadingSongs(false)
                }
            }
        }
    }

    // LOAD FOLDER

    private fun loadSongsFromFolder(
        treeUri: Uri,
        keepCachedSongs: Boolean = false
    ) {
        scanJob?.cancel()

        if (!keepCachedSongs) {
            uiController.setSongs(emptyList())
        }

        uiController.setLoadingSongs(true)

        scanJob = lifecycleScope.launch {
            val startTime = System.currentTimeMillis()

            try {
                val songs = withContext(Dispatchers.IO) {
                    scanFolderFast(treeUri) { batch ->
                        withContext(Dispatchers.Main.immediate) {
                            uiController.appendSongs(batch)
                        }
                    }
                }

                currentCoroutineContext().ensureActive()

                uiController.setAllSongsPreservingSelection(songs)
                uiController.saveCachedSongs(songs)

                audioController.setPlaylist(uiController.state.value.songs)

                audioController.restoreSong(
                    songUri = getLastSong(),
                    autoplay = false
                )

                val elapsed = System.currentTimeMillis() - startTime
                Log.d(
                    TAG,
                    "SCAN FINISHED: ${songs.size} songs in $elapsed ms"
                )
            } catch (e: CancellationException) {
                Log.d(TAG, "Scan cancelled")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "ERROR while scanning folder", e)
                uiController.setLoadingSongs(false)
            }
        }
    }

    // FAST DOCUMENTS CONTRACT SCANNER

    private suspend fun scanFolderFast(
        treeUri: Uri,
        onBatch: suspend (List<Song>) -> Unit
    ): List<Song> {

        val songs = ArrayList<Song>(2048)
        val pendingSongs = ArrayList<Song>(250)

        data class FolderTask(val uri: Uri, val path: String?)

        val folders = ArrayDeque<FolderTask>()

        val rootDocumentUri =
            DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

        folders.addLast(FolderTask(rootDocumentUri, null))

        var processedEntries = 0

        while (folders.isNotEmpty()) {
            currentCoroutineContext().ensureActive()

            val task = folders.removeFirst()
            val currentPath = task.path

            val folderDocumentId =
                DocumentsContract.getDocumentId(task.uri)

            val childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    folderDocumentId
                )

            try {
                contentResolver.query(
                    childrenUri,
                    documentProjection,
                    null,
                    null,
                    null
                )?.use { cursor ->

                    val idColumn = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID
                    )
                    val nameColumn = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    )
                    val mimeColumn = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                    )

                    if (idColumn < 0 || nameColumn < 0 || mimeColumn < 0) {
                        return@use
                    }

                    while (cursor.moveToNext()) {
                        processedEntries++

                        if (processedEntries % 128 == 0) {
                            currentCoroutineContext().ensureActive()
                        }

                        val documentId = cursor.getString(idColumn)
                            ?: continue

                        val displayName = cursor.getString(nameColumn)
                            ?: continue

                        val mimeType = cursor.getString(mimeColumn) ?: ""

                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                            val childUri =
                                DocumentsContract.buildDocumentUriUsingTree(
                                    treeUri,
                                    documentId
                                )

                            val newPath = if (currentPath.isNullOrEmpty()) {
                                displayName
                            } else {
                                "$currentPath / $displayName"
                            }

                            folders.addLast(FolderTask(childUri, newPath))
                            continue
                        }

                        val extension = displayName
                            .substringAfterLast('.', "")
                            .lowercase()

                        if (extension !in audioExtensions) {
                            continue
                        }

                        val fileUri =
                            DocumentsContract.buildDocumentUriUsingTree(
                                treeUri,
                                documentId
                            )

                        val song = Song(
                            title = displayName.substringBeforeLast('.'),
                            assetPath = fileUri.toString(),
                            folderName = currentPath
                        )

                        songs.add(song)
                        pendingSongs.add(song)

                        if (pendingSongs.size >= 250) {
                            val batch = pendingSongs.toList()
                            pendingSongs.clear()

                            onBatch(batch)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                Log.w(TAG, "Permission denied for: ${task.uri}", e)
            } catch (e: Exception) {
                Log.w(TAG, "Could not scan folder: ${task.uri}", e)
            }
        }

        if (pendingSongs.isNotEmpty()) {
            onBatch(pendingSongs.toList())
            pendingSongs.clear()
        }

        songs.sortBy { it.title.lowercase() }

        return songs.distinctBy { it.assetPath }
    }

    // STOP

    override fun onStop() {
        super.onStop()
        if (!uiController.state.value.playInBackground) {
            audioController.pause()
        }
    }

    // DESTROY

    override fun onDestroy() {
        scanJob?.cancel()
        audioController.release()
        super.onDestroy()
    }
}