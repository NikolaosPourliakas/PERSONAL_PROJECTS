package com.example.mymp3player.player

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mymp3player.PlayerState
import com.example.mymp3player.Song
import com.example.mymp3player.UIController
import com.example.mymp3player.audio.ButtonsController
import com.example.mymp3player.playlists.PlaylistController
import com.example.mymp3player.settings.SettingsPage
import com.example.mymp3player.themes.AllInitialThemes
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.themes_setup.CyberAnimation
import com.example.mymp3player.themes.themes_setup.DiscoAnimation
import com.example.mymp3player.themes.themes_setup.ConcertAnimation
import com.example.mymp3player.themes.themes_setup.PinballAnimation
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.mymp3player.Playlist
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight

private data class SourceFolderNode(
    val name: String,
    val path: String,
    val songs: List<Song>,
    val children: List<SourceFolderNode>
)

private fun buildSourceFolderTree(
    songs: List<Song>
): List<SourceFolderNode> {
    class MutableNode(
        val name: String,
        val path: String,
        val songs: MutableList<Song> = mutableListOf(),
        val children: LinkedHashMap<String, MutableNode> = linkedMapOf()
    )

    val roots = linkedMapOf<String, MutableNode>()

    songs.forEach { song ->
        val folderName = song.folderName
            ?.takeIf { it.isNotBlank() }
            ?: return@forEach

        val parts = folderName.split(" / ")
            .filter { it.isNotBlank() }

        var currentMap = roots
        var currentPath = ""

        parts.forEachIndexed { index, part ->
            currentPath = if (currentPath.isEmpty()) {
                part
            } else {
                "$currentPath / $part"
            }

            val node = currentMap.getOrPut(part) {
                MutableNode(part, currentPath)
            }

            if (index == parts.lastIndex) {
                node.songs.add(song)
            }

            currentMap = node.children
        }
    }

    fun convert(nodes: Collection<MutableNode>): List<SourceFolderNode> =
        nodes.map { node ->
            SourceFolderNode(
                name = node.name,
                path = node.path,
                songs = node.songs,
                children = convert(node.children.values)
            )
        }

    return convert(roots.values)
}

@Composable
private fun SourceFolderPicker(
    songs: List<Song>,
    initiallySelectedPaths: Set<String>,
    onDismiss: () -> Unit,
    onApply: (List<Song>, Set<String>) -> Unit
) {
    val folderTree = remember(songs) {
        buildSourceFolderTree(songs)
    }

    var expandedPaths by remember {
        mutableStateOf(setOf<String>())
    }

    var selectedPaths by remember {
        mutableStateOf(initiallySelectedPaths)
    }

    fun toggleFolder(node: SourceFolderNode) {
        val paths = node.allSongs().map { it.assetPath }.toSet()

        selectedPaths = if (paths.isNotEmpty() && paths.all { it in selectedPaths }) {
            selectedPaths - paths
        } else {
            selectedPaths + paths
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Folders") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = folderTree,
                    key = { it.path }
                ) { node ->
                    SourceFolderRow(
                        node = node,
                        depth = 0,
                        expandedPaths = expandedPaths,
                        selectedPaths = selectedPaths,
                        onToggleExpand = { path ->
                            expandedPaths =
                                if (path in expandedPaths) {
                                    expandedPaths - path
                                } else {
                                    expandedPaths + path
                                }
                        },
                        onToggleFolder = ::toggleFolder,
                        onToggleSong = { song ->
                            selectedPaths =
                                if (song.assetPath in selectedPaths) {
                                    selectedPaths - song.assetPath
                                } else {
                                    selectedPaths + song.assetPath
                                }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedSongs = songs.filter {
                        it.assetPath in selectedPaths
                    }

                    onApply(selectedSongs, selectedPaths)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SourcePlaylistPicker(
    playlists: List<Playlist>,
    initiallySelectedPaths: Set<String>,
    onDismiss: () -> Unit,
    onApply: (List<Song>, Set<String>) -> Unit
) {
    var expandedPlaylists by remember {
        mutableStateOf(setOf<String>())
    }

    var selectedPaths by remember {
        mutableStateOf(initiallySelectedPaths)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playlists") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(
                    items = playlists,
                    key = { it.name }
                ) { playlist ->
                    val expanded = playlist.name in expandedPlaylists
                    val allSelected = playlist.songs.isNotEmpty() &&
                            playlist.songs.all { it.assetPath in selectedPaths }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { checked ->
                                    val paths = playlist.songs
                                        .map { it.assetPath }
                                        .toSet()

                                    selectedPaths = if (checked) {
                                        selectedPaths + paths
                                    } else {
                                        selectedPaths - paths
                                    }
                                }
                            )

                            Text(
                                text = if (expanded) "▼" else "▶",
                                modifier = Modifier
                                    .clickable {
                                        expandedPlaylists =
                                            if (expanded) {
                                                expandedPlaylists - playlist.name
                                            } else {
                                                expandedPlaylists + playlist.name
                                            }
                                    }
                                    .padding(8.dp)
                            )

                            Text(
                                text = playlist.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        expandedPlaylists =
                                            if (expanded) {
                                                expandedPlaylists - playlist.name
                                            } else {
                                                expandedPlaylists + playlist.name
                                            }
                                    }
                                    .padding(vertical = 10.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (expanded) {
                            playlist.songs.forEach { song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = song.assetPath in selectedPaths,
                                        onCheckedChange = { checked ->
                                            selectedPaths =
                                                if (checked) {
                                                    selectedPaths + song.assetPath
                                                } else {
                                                    selectedPaths - song.assetPath
                                                }
                                        }
                                    )

                                    Text(
                                        text = song.title,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedPaths =
                                                    if (song.assetPath in selectedPaths) {
                                                        selectedPaths - song.assetPath
                                                    } else {
                                                        selectedPaths + song.assetPath
                                                    }
                                            }
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedSongs = playlists
                        .flatMap { it.songs }
                        .distinctBy { it.assetPath }
                        .filter { it.assetPath in selectedPaths }

                    onApply(selectedSongs, selectedPaths)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SourceFolderRow(
    node: SourceFolderNode,
    depth: Int,
    expandedPaths: Set<String>,
    selectedPaths: Set<String>,
    onToggleExpand: (String) -> Unit,
    onToggleFolder: (SourceFolderNode) -> Unit,
    onToggleSong: (Song) -> Unit
) {
    val folderSongs = remember(node) {
        node.allSongs()
    }

    val allSelected = folderSongs.isNotEmpty() &&
            folderSongs.all { it.assetPath in selectedPaths }

    val expanded = node.path in expandedPaths

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = allSelected,
                onCheckedChange = { onToggleFolder(node) }
            )

            Text(
                text = if (expanded) "▼" else "▶",
                modifier = Modifier
                    .clickable { onToggleExpand(node.path) }
                    .padding(8.dp)
            )

            Text(
                text = "📁 ${node.name}",
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleExpand(node.path) }
                    .padding(vertical = 10.dp)
            )
        }

        if (expanded) {
            node.songs.forEach { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = ((depth + 1) * 16).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = song.assetPath in selectedPaths,
                        onCheckedChange = { onToggleSong(song) }
                    )

                    Text(
                        text = song.title,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleSong(song) }
                            .padding(vertical = 8.dp)
                    )
                }
            }

            node.children.forEach { child ->
                SourceFolderRow(
                    node = child,
                    depth = depth + 1,
                    expandedPaths = expandedPaths,
                    selectedPaths = selectedPaths,
                    onToggleExpand = onToggleExpand,
                    onToggleFolder = onToggleFolder,
                    onToggleSong = onToggleSong
                )
            }
        }
    }
}

private fun SourceFolderNode.allSongs(): List<Song> =
    songs + children.flatMap { it.allSongs() }

@SuppressLint("FrequentlyChangingValue")
@Composable
fun PlayerScreen(
    state: PlayerState,
    currentSong: Song?,
    buttons: ButtonsController,
    uiController: UIController,
    playlistController: PlaylistController,
    onMusicFolderClick: () -> Unit
) {
    val listState = rememberLazyListState()

    var showSettings by remember {
        mutableStateOf(false)
    }
    var page by remember {
        mutableStateOf(SettingsPage.MAIN)
    }
    var editingColorKey by remember {
        mutableStateOf<String?>(null)
    }
    var isPreview by remember {
        mutableStateOf(false)
    }
    var pendingTheme by remember {
        mutableStateOf(state.theme)
    }
    var baseTheme by remember {
        mutableStateOf(state.theme)
    }
    var selectedPlaylistName by remember {
        mutableStateOf<String?>(null)
    }
    var showSearch by remember {
        mutableStateOf(false)
    }
    var searchQuery by remember {
        mutableStateOf("")
    }
    var showSourceMenu by remember {
        mutableStateOf(false)
    }

    var showFolderPicker by remember {
        mutableStateOf(false)
    }

    var showPlaylistPicker by remember {
        mutableStateOf(false)
    }

    var selectedPlaylistSongPaths by remember {
        mutableStateOf(uiController.loadSelectedSongPaths())
    }

    var selectedFolderSongPaths by remember {
        mutableStateOf(uiController.loadSelectedSongPaths())
    }

    /* ----- CURRENT SOURCE ----- */

    var currentSourceName by remember {
        mutableStateOf(uiController.loadLastSource())
    }

    /* ----- SELECTED THEME ----- */

    val selectedTheme =
        if (showSettings) {
            pendingTheme
        } else {
            state.theme
        }
    val selectedThemeName =
        AllInitialThemes
            .firstOrNull { item ->
                item.theme == selectedTheme
            }
            ?.name
            ?: "Custom"

    /* ----- THEME ANIMATION ----- */

    val animationEnabled = state.themeAnimationsEnabled.contains(selectedThemeName)

    val isPinball = selectedTheme.animation === PinballAnimation && animationEnabled

    val theme =
        if (isPinball) {
            PinballAnimation.animatedTheme(selectedTheme)
        } else {
            rememberAnimatedTheme(
                theme = selectedTheme,
                enabled = animationEnabled
            )
        }

    fun contrastColor(background: Color): Color {
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

    /* ----- SETTINGS PANEL COLORS ----- */

    val settingsPanelBackground = Color(0xFFF5F5F5)
    val settingsPanelText = Color(0xFF202124)
    val settingsPanelBorder = Color(0xFFBDBDBD)

    /* ----- CURRENT SONG ----- */

    val actualCurrentSongUri = buttons.getCurrentSongUri()

    val actualCurrentSong =
        remember(
            actualCurrentSongUri,
            state.allSongs
        ) {
            state.allSongs.firstOrNull {
                it.assetPath == actualCurrentSongUri
            } ?: currentSong
        }

    /* ----- SOURCE COLORS ----- */

    val allSongsColor = theme.allSongsColor
    val playlistColor = theme.playlistColor
    val folderColor = theme.folderColor
    val isAllSongsSelected = currentSourceName == "All Songs"
    val isFolderSelected = currentSourceName.startsWith("Folder:")
    val isPlaylistSelected = currentSourceName.startsWith("Playlist:")

    /* ----- SEARCH ----- */

    val filteredSongs =
        remember(state.songs, searchQuery) {
            if (searchQuery.isBlank()) {
                state.songs
            } else {
                state.songs.filter { song ->
                    song.title.contains(
                        searchQuery,
                        ignoreCase = true
                    )
                }
            }
        }

    /* ----- CURRENT SONG INDEX ----- */

    val currentSongAssetPath = actualCurrentSong?.assetPath

    val filteredCurrentIndex =
        if (currentSongAssetPath == null) {
            -1
        } else {
            filteredSongs.indexOfFirst {
                it.assetPath == currentSongAssetPath
            }
        }

    /* ----- CLOSE SETTINGS ----- */

    fun closeSettings() {
        pendingTheme = baseTheme
        showSettings = false
        isPreview = false
        page = SettingsPage.MAIN
        selectedPlaylistName = null
        editingColorKey = null
    }

    /* ----- BACK ----- */

    BackHandler(enabled = showSettings) {
        if (isPreview) {
            isPreview = false
        } else {
            closeSettings()
        }
    }

    /* ----- PREVIEW ----- */

    if (showSettings && isPreview) {
        PlayerPreview(
            state = state,
            currentSong = actualCurrentSong,
            theme = theme,
            currentSourceName = currentSourceName,
            isAllSongsSelected = isAllSongsSelected,
            isFolderSelected = isFolderSelected,
            isPlaylistSelected = isPlaylistSelected,
            allSongsColor = allSongsColor,
            folderColor = folderColor,
            playlistColor = playlistColor,
            filteredSongs = filteredSongs,
            currentSongAssetPath = currentSongAssetPath,
            filteredCurrentIndex = filteredCurrentIndex,
            listState = listState,
            onClose = {
                isPreview = false
            }
        )
    } else {

        /* ----- MAIN PLAYER ----- */

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        theme.screenBackground
                    )
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp)
            ) {

                /* ----- HEADER ----- */

                PlayerHeader(
                    currentSong = actualCurrentSong,
                    theme = theme,
                    isRandom = state.isRandom,
                    onShuffleClick = {
                        buttons.setRandom(!state.isRandom)
                    },
                    onSettingsClick = {
                        showSettings = true
                        baseTheme = state.theme
                        pendingTheme = baseTheme
                        isPreview = false
                        page = SettingsPage.MAIN
                    }
                )

                /* ----- SOURCE / SEARCH ROW ----- */

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSourceMenu = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Επιλογή πηγής",
                            tint = theme.sourceIcon
                        )

                        Text(
                            text = currentSourceName,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            color = when {
                                isAllSongsSelected -> allSongsColor
                                isFolderSelected -> folderColor
                                isPlaylistSelected -> playlistColor
                                else -> theme.listText
                            },
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )

                        DropdownMenu(
                            expanded = showSourceMenu,
                            onDismissRequest = { showSourceMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Songs") },
                                onClick = {
                                    currentSourceName = "All Songs"
                                    selectedFolderSongPaths = emptySet()
                                    selectedPlaylistSongPaths = emptySet()

                                    uiController.saveLastSource("All Songs")
                                    uiController.saveSelectedSongPaths(emptySet())

                                    uiController.setDisplayedSongs(state.allSongs)
                                    buttons.setPlaylist(state.allSongs)
                                    showSourceMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Folders") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showSourceMenu = false
                                    showFolderPicker = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Playlists") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showSourceMenu = false
                                    showPlaylistPicker = true
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (showSearch) {
                                searchQuery = ""
                                showSearch = false
                            } else {
                                showSearch = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (showSearch) {
                                Icons.Default.Close
                            } else {
                                Icons.Default.Search
                            },
                            contentDescription = if (showSearch) "Close search" else "Search",
                            tint = theme.listText
                        )
                    }
                }

                /* ----- SEARCH BAR ----- */

                if (showSearch) {
                    PlayerSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = {
                            searchQuery = it
                        },
                        theme = theme
                    )
                }

                /* ----- SONG LIST ----- */

                Row(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                ) {

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .border(
                                    2.dp,
                                    theme.listBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    theme.listBackground,
                                    RoundedCornerShape(12.dp)
                                )
                    ) {

                        PlayerSongList(
                            songs = filteredSongs,
                            allDisplayedSongs = state.songs,
                            currentSongUri = currentSongAssetPath,
                            theme = theme,
                            listState = listState,
                            onSongClick = { index ->
                                buttons.selectSong(index)
                            }
                        )
                    }

                    /* ----- SCROLLBAR ----- */

                    val visibleItems =
                        listState
                            .layoutInfo
                            .visibleItemsInfo
                            .size

                    PlayerScrollbar(
                        totalItems = filteredSongs.size,
                        visibleItems = visibleItems,
                        firstVisibleItemIndex = listState.firstVisibleItemIndex,
                        currentItemIndex = filteredCurrentIndex,
                        onScrollToIndex = {
                            listState.scrollToItem(it)
                        },
                        trackColor = theme.scrollTrack,
                        thumbColor = theme.scrollThumb
                    )
                }

                Spacer(Modifier.height(10.dp))

                /* ----- SONG PROGRESS ----- */

                SongProgressBar(
                    position = state.currentPosition,
                    duration = state.duration,
                    onSeek = {
                        buttons.seekToPosition(it)
                    },
                    trackColor = theme.songBarTrack,
                    thumbColor = theme.songBarThumb,
                    timeTextColor = theme.songTimeText
                )

                Spacer(Modifier.height(4.dp))

                /* ----- CONTROLS ----- */

                PlayerControls(
                    isPlaying = state.isPlaying,
                    theme = theme,
                    onPrevious = {
                        buttons.previous()
                    },
                    onPlayPause = {
                        buttons.playPause(state.isPlaying)
                    },
                    onNext = {
                        buttons.next()
                    }
                )
            }

            DiscoAnimation.LightSpotOverlay(uiController = uiController)
            CyberAnimation.CircuitOverlay(uiController = uiController)
            ConcertAnimation.StageLights(uiController = uiController)
            PinballAnimation.LightOverlay(uiController = uiController)

            /* ----- SETTINGS OVERLAY ----- */

            if (showSettings) {

                PlayerSettingsOverlay(
                    state = state,
                    pendingTheme = pendingTheme,
                    page = page,
                    selectedPlaylistName = selectedPlaylistName,
                    editingColorKey = editingColorKey,
                    playlistsFlow = playlistController.playlists,
                    playlistController = playlistController,
                    uiController = uiController,
                    settingsPanelBackground = settingsPanelBackground,
                    settingsPanelText = settingsPanelText,
                    settingsPanelBorder = settingsPanelBorder,
                    isPlaylistSelected = isPlaylistSelected,
                    onPageChange = {
                        page = it
                    },
                    onSelectedPlaylistChange = {
                        selectedPlaylistName = it
                    },
                    onClose = {
                        closeSettings()
                    },
                    onMusicFolderClick = {
                        onMusicFolderClick()
                    },
                    onThemeSelected = {
                            selectedTheme -> uiController.setTheme(selectedTheme)
                        pendingTheme = selectedTheme
                        baseTheme = selectedTheme
                    },
                    onPreviewTemplate = {
                        pendingTheme = it
                        isPreview = true
                    },
                    onEditColor = {
                        editingColorKey = it
                    },
                    onColorChanged = {
                            key,
                            color -> pendingTheme = pendingTheme.copyColor(
                                key = key,
                                color = color
                            )
                        editingColorKey = null
                    },

                    onPlayPlaylist = {
                            playlist ->
                        if (playlist.songs.isNotEmpty()) {
                            currentSourceName = "Playlist: ${playlist.name}"
                            uiController.saveLastSource(currentSourceName)
                            uiController.saveSelectedSongPaths(
                                playlist.songs.map { it.assetPath }.toSet()
                            )
                            uiController.setDisplayedSongs(playlist.songs)
                            buttons.setPlaylist(playlist.songs)
                            buttons.selectSong(0)
                            showSettings = false
                            page = SettingsPage.MAIN
                            selectedPlaylistName = null
                        }
                    }
                )
            }
        }
    }

    if (showFolderPicker) {
        SourceFolderPicker(
            songs = state.allSongs,
            initiallySelectedPaths = selectedFolderSongPaths,
            onDismiss = {
                showFolderPicker = false
            },
            onApply = { selectedSongs, selectedPaths ->
                selectedFolderSongPaths = selectedPaths

                if (selectedSongs.isEmpty()) {
                    currentSourceName = "All Songs"
                    selectedFolderSongPaths = emptySet()
                    selectedPlaylistSongPaths = emptySet()

                    uiController.saveLastSource("All Songs")
                    uiController.saveSelectedSongPaths(emptySet())

                    uiController.setDisplayedSongs(state.allSongs)
                    buttons.setPlaylist(state.allSongs)
                } else {
                    currentSourceName = "Folder: ${selectedSongs.size} τραγούδια"

                    uiController.saveLastSource(currentSourceName)
                    uiController.saveSelectedSongPaths(selectedPaths)

                    uiController.setDisplayedSongs(selectedSongs)
                    buttons.setPlaylist(selectedSongs)
                }

                showFolderPicker = false
            }
        )
    }

    if (showPlaylistPicker) {
        val playlists by playlistController.playlists.collectAsState(
            initial = emptyList()
        )

        SourcePlaylistPicker(
            playlists = playlists,
            initiallySelectedPaths = selectedPlaylistSongPaths,
            onDismiss = {
                showPlaylistPicker = false
            },
            onApply = { selectedSongs, selectedPaths ->
                selectedPlaylistSongPaths = selectedPaths

                if (selectedSongs.isEmpty()) {
                    currentSourceName = "All Songs"
                    selectedFolderSongPaths = emptySet()
                    selectedPlaylistSongPaths = emptySet()

                    uiController.saveLastSource("All Songs")
                    uiController.saveSelectedSongPaths(emptySet())

                    uiController.setDisplayedSongs(state.allSongs)
                    buttons.setPlaylist(state.allSongs)
                } else {
                    currentSourceName = "Επιλεγμένα τραγούδια: ${selectedSongs.size}"

                    uiController.saveLastSource(currentSourceName)
                    uiController.saveSelectedSongPaths(selectedPaths)

                    uiController.setDisplayedSongs(selectedSongs)
                    buttons.setPlaylist(selectedSongs)
                }

                showPlaylistPicker = false
            }
        )
    }
}

/* ----- COPY COLOR ----- */

private fun AppTheme.copyColor(
    key: String,
    color: Color
): AppTheme {

    return when (key) {
        "screenBackground" -> copy(screenBackground = color)
        "headerBackground" -> copy(headerBackground = color)
        "headerBorder" -> copy(headerBorder = color)
        "headerTitleText" -> copy(headerTitleText = color)
        "headerSongText" -> copy(headerSongText = color)
        "settingsIcon" -> copy(settingsIcon = color)
        "sourceButtonColor" -> copy(sourceButtonColor = color)
        "settingsButtonColor" -> copy(settingsButtonColor = color)
        "settingsBackground" -> copy(settingsBackground = color)
        "settingsText" -> copy(settingsText = color)
        "settingsSecondaryText" -> copy(settingsSecondaryText = color)
        "listBackground" -> copy(listBackground = color)
        "listBorder" -> copy(listBorder = color)
        "listText" -> copy(listText = color)
        "activeSongColor" -> copy(activeSongColor = color)
        "allSongsColor" -> copy(allSongsColor = color)
        "folderColor" -> copy(folderColor = color)
        "playlistColor" -> copy(playlistColor = color)
        "sourceIcon" -> copy(sourceIcon = color)
        "randomActive" -> copy(randomActive = color)
        "randomInactive" -> copy(randomInactive = color)
        "scrollTrack" -> copy(scrollTrack = color)
        "scrollThumb" -> copy(scrollThumb = color)
        "songBarTrack" -> copy(songBarTrack = color)
        "songBarProgress" -> copy(songBarProgress = color)
        "songBarThumb" -> copy(songBarThumb = color)
        "songTimeText" -> copy(songTimeText = color)
        "prevBg" -> copy(prevBg = color)
        "prevBorder" -> copy(prevBorder = color)
        "prevIcon" -> copy(prevIcon = color)
        "playBg" -> copy(playBg = color)
        "playBorder" -> copy(playBorder = color)
        "playIcon" -> copy(playIcon = color)
        "nextBg" -> copy(nextBg = color)
        "nextBorder" -> copy(nextBorder = color)
        "nextIcon" -> copy(nextIcon = color)
        else -> this
    }
}