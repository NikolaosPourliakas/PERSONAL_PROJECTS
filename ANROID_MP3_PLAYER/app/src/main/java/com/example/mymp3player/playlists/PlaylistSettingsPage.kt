package com.example.mymp3player.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymp3player.Playlist
import com.example.mymp3player.Song
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class PlaylistFolderNode(
    val name: String,
    val path: String,
    val songs: List<Song>,
    val children: List<PlaylistFolderNode>
)

private fun buildPlaylistFolderTree(songs: List<Song>): List<PlaylistFolderNode> {
    data class MutableNode(
        val name: String,
        val path: String,
        val songs: MutableList<Song> = mutableListOf(),
        val children: LinkedHashMap<String, MutableNode> = linkedMapOf()
    )

    val roots = linkedMapOf<String, MutableNode>()

    songs.forEach { song ->
        val folderPath = song.folderName ?: return@forEach
        if (folderPath.isBlank()) return@forEach

        val parts = folderPath.split(" / ").filter { it.isNotBlank() }
        var currentMap = roots
        var currentPath = ""

        parts.forEachIndexed { index, part ->
            currentPath = if (currentPath.isEmpty()) {
                part
            } else {
                "$currentPath / $part"
            }

            val node = currentMap[part] ?: MutableNode(
                name = part,
                path = currentPath
            ).also {
                currentMap[part] = it
            }

            if (index == parts.lastIndex) {
                node.songs.add(song)
            }

            currentMap = node.children
        }
    }

    fun convert(nodes: Collection<MutableNode>): List<PlaylistFolderNode> {
        return nodes.map { node ->
            PlaylistFolderNode(
                name = node.name,
                path = node.path,
                songs = node.songs.sortedBy { it.title.lowercase() },
                children = convert(node.children.values)
            )
        }
    }

    return convert(roots.values)
}

private fun findSongFolderPath(
    nodes: List<PlaylistFolderNode>,
    assetPath: String
): String? {
    for (node in nodes) {
        if (node.songs.any { it.assetPath == assetPath }) {
            return node.path
        }

        val childPath = findSongFolderPath(node.children, assetPath)
        if (childPath != null) return childPath
    }

    return null
}

private sealed class PlaylistListItem {
    data class Folder(
        val node: PlaylistFolderNode,
        val depth: Int
    ) : PlaylistListItem()

    data class Track(
        val song: Song,
        val folderPath: String
    ) : PlaylistListItem()

    data class TrackWithoutFolder(
        val song: Song
    ) : PlaylistListItem()
}

private fun flattenPlaylistFolders(
    nodes: List<PlaylistFolderNode>,
    expandedFolders: Set<String>,
    songsWithoutFolder: List<Song>
): List<PlaylistListItem> {
    val result = mutableListOf<PlaylistListItem>()

    fun visit(node: PlaylistFolderNode, depth: Int) {
        result.add(PlaylistListItem.Folder(node, depth))

        if (node.path in expandedFolders) {
            node.songs.forEach { song ->
                result.add(
                    PlaylistListItem.Track(
                        song = song,
                        folderPath = node.path
                    )
                )
            }

            node.children.forEach { child ->
                visit(child, depth + 1)
            }
        }
    }

    nodes.forEach { visit(it, 0) }

    songsWithoutFolder.forEach { song ->
        result.add(PlaylistListItem.TrackWithoutFolder(song))
    }

    return result
}

@Composable
fun PlaylistSettingsPage(
    playlists: List<Playlist>,
    playlistGroups: List<PlaylistGroup>,
    allSongs: List<Song>,
    selectedPlaylistName: String?,
    onSelectPlaylist: (String) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onAddSong: (String, Song) -> Unit,
    onRemoveSong: (String, Song) -> Unit,
    onAddSongs: (String, List<Song>) -> Unit,
    onRemoveSongs: (String, List<Song>) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onCreatePlaylistGroup: (String) -> Unit,
    onDeletePlaylistGroup: (String) -> Unit,
    onAddPlaylistToGroup: (String, String) -> Unit,
    onRemovePlaylistFromGroup: (String, String) -> Unit,
) {
    val settingsBackground = Color(0xFFF5F5F5)
    val settingsText = Color(0xFF202124)
    val settingsSecondaryText = Color(0xFF5F6368)
    val settingsActive = Color(0xFF546E7A)
    val settingsActiveBackground = Color(0xFFECEFF1)
    val settingsBorder = Color(0xFFBDBDBD)

    var newPlaylistName by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("all") }
    var newGroupName by remember { mutableStateOf("") }
    var selectedGroupName by remember { mutableStateOf<String?>(null) }
    var openedPlaylistName by remember { mutableStateOf<String?>(null) }
    var openedGroupName by remember { mutableStateOf<String?>(null) }
    var expandedGroupPlaylists by remember { mutableStateOf(setOf<String>()) }

    val selectedPlaylist = playlists.firstOrNull { it.name == selectedPlaylistName }
    val openedPlaylist = playlists.firstOrNull { it.name == openedPlaylistName }
    val openedGroup = playlistGroups.firstOrNull { it.name == openedGroupName }

    val folderTree = remember(allSongs) {
        buildPlaylistFolderTree(allSongs)
    }

    val songsWithoutFolder = remember(allSongs) {
        allSongs
            .filter { it.folderName.isNullOrBlank() }
            .sortedBy { it.title.lowercase() }
    }

    var expandedFolders by remember {
        mutableStateOf(setOf<String>())
    }

    var currentFolderPath by remember {
        mutableStateOf<String?>(null)
    }

    val folderListState = remember { LazyListState() }

    var visibleFolderPath by remember {
        mutableStateOf<String?>(null)
    }

    val visibleFolderNames = visibleFolderPath
        ?.split(" / ")
        ?.filter { it.isNotBlank() }
        ?.take(3)
        .orEmpty()

    val playlistItems = remember(
        folderTree,
        expandedFolders,
        songsWithoutFolder
    ) {
        flattenPlaylistFolders(
            nodes = folderTree,
            expandedFolders = expandedFolders,
            songsWithoutFolder = songsWithoutFolder
        )
    }

    LaunchedEffect(folderListState, playlistItems) {
        snapshotFlow {
            folderListState.layoutInfo.visibleItemsInfo
                .minByOrNull { it.offset }
                ?.key
                ?.toString()
        }
            .distinctUntilChanged()
            .collect { key ->
                val item = playlistItems.firstOrNull {
                    when (it) {
                        is PlaylistListItem.Folder ->
                            "folder:${it.node.path}" == key

                        is PlaylistListItem.Track ->
                            "song:${it.song.assetPath}" == key

                        is PlaylistListItem.TrackWithoutFolder ->
                            "no-folder:${it.song.assetPath}" == key
                    }
                }

                visibleFolderPath = when (item) {
                    is PlaylistListItem.Folder -> item.node.path
                    is PlaylistListItem.Track -> item.folderPath
                    is PlaylistListItem.TrackWithoutFolder -> null
                    null -> null
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(settingsBackground)
            .padding(20.dp)
    ) {
        Text(
            text = "Playlists",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = settingsText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    selectedSection = "all"
                    openedGroupName = null
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSection == "all") {
                        settingsActive
                    } else {
                        settingsActiveBackground
                    },
                    contentColor = if (selectedSection == "all") {
                        Color.White
                    } else {
                        settingsText
                    }
                )
            ) {
                Text("All")
            }

            Button(
                onClick = {
                    selectedSection = "groups"
                    openedPlaylistName = null
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSection == "groups") {
                        settingsActive
                    } else {
                        settingsActiveBackground
                    },
                    contentColor = if (selectedSection == "groups") {
                        Color.White
                    } else {
                        settingsText
                    }
                )
            ) {
                Text("Groups")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSection == "all") {
            if (openedPlaylistName != null) {
                val playlist = openedPlaylist
                val activePlaylist = playlist ?: return@Column

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            openedPlaylistName = null
                            expandedFolders = emptySet()
                            currentFolderPath = null
                        }
                    ) {
                        Text("← Back")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = playlist?.name ?: openedPlaylistName.orEmpty(),
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = settingsText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (visibleFolderNames.isNotEmpty()) {
                    Text(
                        text = visibleFolderNames.joinToString(" / "),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                settingsActiveBackground,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = settingsText,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (playlist == null) {
                    Text("Playlist not found", color = settingsSecondaryText)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = folderListState,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = playlistItems,
                            key = { item ->
                                when (item) {
                                    is PlaylistListItem.Folder ->
                                        "folder:${item.node.path}"

                                    is PlaylistListItem.Track ->
                                        "song:${item.song.assetPath}"

                                    is PlaylistListItem.TrackWithoutFolder ->
                                        "no-folder:${item.song.assetPath}"
                                }
                            }
                        ) { item ->
                            when (item) {
                                is PlaylistListItem.Folder -> {
                                    PlaylistFolderHeader(
                                        node = item.node,
                                        depth = item.depth,
                                        expandedFolders = expandedFolders,
                                        onToggleFolder = { path ->
                                            expandedFolders =
                                                if (path in expandedFolders) {
                                                    expandedFolders - path
                                                } else {
                                                    expandedFolders + path
                                                }
                                        },
                                        selectedPlaylist = activePlaylist,
                                        onAddSongs = onAddSongs,
                                        onRemoveSongs = onRemoveSongs,
                                        settingsText = settingsText,
                                        settingsSecondaryText = settingsSecondaryText,
                                        settingsActiveBackground = settingsActiveBackground
                                    )
                                }

                                is PlaylistListItem.Track -> {
                                    val song = item.song
                                    val checked = activePlaylist.songs.any {
                                        it.assetPath == song.assetPath
                                    }

                                    PlaylistSongRow(
                                        song = song,
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                onAddSong(activePlaylist.name, song)
                                            } else {
                                                onRemoveSong(activePlaylist.name, song)
                                            }
                                        }
                                    )
                                }

                                is PlaylistListItem.TrackWithoutFolder -> {
                                    val song = item.song
                                    val checked = activePlaylist.songs.any {
                                        it.assetPath == song.assetPath
                                    }

                                    PlaylistSongRow(
                                        song = song,
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                onAddSong(activePlaylist.name, song)
                                            } else {
                                                onRemoveSong(activePlaylist.name, song)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = {
                            Text(
                                text = "Playlist Name",
                                color = settingsSecondaryText
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = settingsText,
                            unfocusedTextColor = settingsText,
                            focusedBorderColor = settingsActive,
                            unfocusedBorderColor = settingsBorder,
                            focusedLabelColor = settingsActive,
                            unfocusedLabelColor = settingsSecondaryText,
                            cursorColor = settingsActive
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val name = newPlaylistName.trim()
                            if (name.isNotEmpty()) {
                                onCreatePlaylist(name)
                                newPlaylistName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = settingsActive,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No Playlists Found",
                        modifier = Modifier.padding(8.dp),
                        color = settingsSecondaryText
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = playlists,
                            key = { it.name }
                        ) { playlist ->
                            val isSelected = playlist.name == selectedPlaylistName

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) {
                                            settingsActiveBackground
                                        } else {
                                            Color.White
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onSelectPlaylist(playlist.name)
                                        openedPlaylistName = playlist.name
                                        expandedFolders = emptySet()
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = playlist.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) {
                                            settingsActive
                                        } else {
                                            settingsText
                                        }
                                    )

                                    Text(
                                        text = "${playlist.songs.size} Songs",
                                        fontSize = 12.sp,
                                        color = settingsSecondaryText
                                    )
                                }

                                IconButton(
                                    onClick = { onPlayPlaylist(playlist) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = settingsText
                                    )
                                }

                                Spacer(modifier = Modifier.width(2.dp))

                                Button(
                                    onClick = { onDeletePlaylist(playlist.name) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = settingsActive,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("🗑")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (openedGroupName != null) {
                val group = openedGroup

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            openedGroupName = null
                            expandedGroupPlaylists = emptySet()
                        }
                    ) {
                        Text("← Back")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = group?.name ?: openedGroupName.orEmpty(),
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = settingsText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (group == null) {
                    Text("Group not found", color = settingsSecondaryText)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = playlists,
                            key = { it.name }
                        ) { playlist ->
                            val isAdded = playlist.name in group.playlistNames
                            val isExpanded = playlist.name in expandedGroupPlaylists

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isAdded) settingsActiveBackground else Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isAdded,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                onAddPlaylistToGroup(group.name, playlist.name)
                                            } else {
                                                onRemovePlaylistFromGroup(group.name, playlist.name)
                                            }
                                        }
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                expandedGroupPlaylists =
                                                    if (isExpanded) {
                                                        expandedGroupPlaylists - playlist.name
                                                    } else {
                                                        expandedGroupPlaylists + playlist.name
                                                    }
                                            }
                                    ) {
                                        Text(
                                            text = playlist.name,
                                            fontWeight = FontWeight.Bold,
                                            color = settingsText
                                        )
                                        Text(
                                            text = "${playlist.songs.size} Songs",
                                            fontSize = 12.sp,
                                            color = settingsSecondaryText
                                        )
                                    }

                                    Text(
                                        text = if (isExpanded) "▼" else "▶",
                                        modifier = Modifier
                                            .clickable {
                                                expandedGroupPlaylists =
                                                    if (isExpanded) {
                                                        expandedGroupPlaylists - playlist.name
                                                    } else {
                                                        expandedGroupPlaylists + playlist.name
                                                    }
                                            }
                                            .padding(8.dp),
                                        color = settingsText
                                    )

                                    IconButton(
                                        onClick = { onPlayPlaylist(playlist) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = settingsText
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    playlist.songs.forEach { song ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 48.dp,
                                                    top = 4.dp,
                                                    bottom = 4.dp
                                                )
                                        ) {
                                            Text(
                                                text = song.title,
                                                color = settingsText
                                            )

                                            Text(
                                                text = song.folderName ?: "No folder",
                                                fontSize = 12.sp,
                                                color = settingsSecondaryText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Group Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = settingsText,
                            unfocusedTextColor = settingsText,
                            focusedBorderColor = settingsActive,
                            unfocusedBorderColor = settingsBorder,
                            cursorColor = settingsActive
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val name = newGroupName.trim()
                            if (name.isNotEmpty()) {
                                onCreatePlaylistGroup(name)
                                selectedGroupName = name
                                newGroupName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = settingsActive,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (playlistGroups.isEmpty()) {
                    Text(
                        text = "No Groups Found",
                        color = settingsSecondaryText
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = playlistGroups,
                            key = { it.name }
                        ) { group ->
                            val isSelected = group.name == selectedGroupName

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) settingsActiveBackground else Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedGroupName = group.name
                                        openedGroupName = group.name
                                        expandedGroupPlaylists = emptySet()
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = group.name,
                                        fontWeight = FontWeight.Bold,
                                        color = settingsText
                                    )
                                    Text(
                                        text = "${group.playlistNames.size} Playlists",
                                        fontSize = 12.sp,
                                        color = settingsSecondaryText
                                    )
                                }

                                Button(
                                    onClick = {
                                        onDeletePlaylistGroup(group.name)
                                        if (selectedGroupName == group.name) {
                                            selectedGroupName = null
                                        }
                                        if (openedGroupName == group.name) {
                                            openedGroupName = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = settingsActive,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("🗑")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistFolderHeader(
    node: PlaylistFolderNode,
    depth: Int,
    expandedFolders: Set<String>,
    onToggleFolder: (String) -> Unit,
    selectedPlaylist: Playlist,
    onAddSongs: (String, List<Song>) -> Unit,
    onRemoveSongs: (String, List<Song>) -> Unit,
    settingsText: Color,
    settingsSecondaryText: Color,
    settingsActiveBackground: Color
) {
    val isExpanded = node.path in expandedFolders
    val allFolderSongs = remember(node) {
        getAllFolderSongs(node)
    }

    val totalSongs = allFolderSongs.size

    val allChecked = allFolderSongs.isNotEmpty() &&
            allFolderSongs.all { song ->
                selectedPlaylist.songs.any {
                    it.assetPath == song.assetPath
                }
            }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                settingsActiveBackground,
                RoundedCornerShape(8.dp)
            )
            .padding(
                start = (depth * 20 + 4).dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = allChecked,
            onCheckedChange = { checked ->
                val selectedPaths = selectedPlaylist.songs
                    .map { it.assetPath }
                    .toHashSet()

                if (checked) {
                    val songsToAdd = allFolderSongs.filter {
                        it.assetPath !in selectedPaths
                    }
                    if (songsToAdd.isNotEmpty()) {
                        onAddSongs(selectedPlaylist.name, songsToAdd)
                    }
                } else {
                    val songsToRemove = allFolderSongs.filter {
                        it.assetPath in selectedPaths
                    }
                    if (songsToRemove.isNotEmpty()) {
                        onRemoveSongs(selectedPlaylist.name, songsToRemove)
                    }
                }
            },
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = if (isExpanded) "▼" else "▶",
            modifier = Modifier
                .clickable { onToggleFolder(node.path) }
                .padding(horizontal = 6.dp),
            color = settingsText
        )

        Text(
            text = "📁 ${node.name}",
            modifier = Modifier
                .weight(1f)
                .clickable { onToggleFolder(node.path) },
            fontWeight = FontWeight.Bold,
            color = settingsText
        )

        Text(
            text = "$totalSongs",
            fontSize = 12.sp,
            color = settingsSecondaryText
        )
    }
}

@Composable
fun PlaylistFolderSection(
    node: PlaylistFolderNode,
    depth: Int,
    expandedFolders: Set<String>,
    onToggleFolder: (String) -> Unit,
    selectedPlaylist: Playlist,
    onAddSong: (String, Song) -> Unit,
    onRemoveSong: (String, Song) -> Unit,
    onAddSongs: (String, List<Song>) -> Unit,
    onRemoveSongs: (String, List<Song>) -> Unit,
    settingsText: Color,
    settingsSecondaryText: Color,
    settingsActiveBackground: Color
) {
    val isExpanded = node.path in expandedFolders

    val totalSongs = node.songs.size +
            node.children.sumOf { countPlaylistFolderSongs(it) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    settingsActiveBackground,
                    RoundedCornerShape(8.dp)
                )
                .padding(
                    start = (depth * 20 + 4).dp,
                    top = 4.dp,
                    bottom = 4.dp,
                    end = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val allFolderSongs = buildList {
                addAll(node.songs)
                node.children.forEach { child ->
                    addAll(getAllFolderSongs(child))
                }
            }

            val allChecked = allFolderSongs.isNotEmpty() &&
                    allFolderSongs.all { song ->
                        selectedPlaylist.songs.any {
                            it.assetPath == song.assetPath
                        }
                    }

            Checkbox(
                checked = allChecked,
                onCheckedChange = { checked ->
                    val selectedPaths = selectedPlaylist.songs
                        .map { it.assetPath }
                        .toHashSet()

                    if (checked) {
                        val songsToAdd = allFolderSongs.filter {
                            it.assetPath !in selectedPaths
                        }
                        if (songsToAdd.isNotEmpty()) {
                            onAddSongs(selectedPlaylist.name, songsToAdd)
                        }
                    } else {
                        val songsToRemove = allFolderSongs.filter {
                            it.assetPath in selectedPaths
                        }
                        if (songsToRemove.isNotEmpty()) {
                            onRemoveSongs(selectedPlaylist.name, songsToRemove)
                        }
                    }
                },
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = if (isExpanded) "▼" else "▶",
                modifier = Modifier
                    .clickable { onToggleFolder(node.path) }
                    .padding(horizontal = 6.dp),
                color = settingsText
            )

            Text(
                text = "📁 ${node.name}",
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleFolder(node.path) },
                fontWeight = FontWeight.Bold,
                color = settingsText
            )

            Text(
                text = "$totalSongs",
                fontSize = 12.sp,
                color = settingsSecondaryText
            )
        }

        if (isExpanded) {
            node.songs.forEach { song ->
                val checked = selectedPlaylist.songs.any {
                    it.assetPath == song.assetPath
                }

                PlaylistSongRow(
                    song = song,
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            onAddSong(selectedPlaylist.name, song)
                        } else {
                            onRemoveSong(selectedPlaylist.name, song)
                        }
                    }
                )
            }

            node.children.forEach { child ->
                PlaylistFolderSection(
                    node = child,
                    depth = depth + 1,
                    expandedFolders = expandedFolders,
                    onToggleFolder = onToggleFolder,
                    selectedPlaylist = selectedPlaylist,
                    onAddSong = onAddSong,
                    onRemoveSong = onRemoveSong,
                    onAddSongs = onAddSongs,
                    onRemoveSongs = onRemoveSongs,
                    settingsText = settingsText,
                    settingsSecondaryText = settingsSecondaryText,
                    settingsActiveBackground = settingsActiveBackground
                )
            }
        }
    }
}

private fun countPlaylistFolderSongs(node: PlaylistFolderNode): Int {
    return node.songs.size +
            node.children.sumOf { countPlaylistFolderSongs(it) }
}

private fun getAllFolderSongs(node: PlaylistFolderNode): List<Song> {
    return node.songs + node.children.flatMap { getAllFolderSongs(it) }
}