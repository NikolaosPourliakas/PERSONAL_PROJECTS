package com.example.mymp3player.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mymp3player.PlayerState
import com.example.mymp3player.Playlist
import com.example.mymp3player.UIController
import com.example.mymp3player.playlists.PlaylistController
import com.example.mymp3player.playlists.PlaylistSettingsPage
import com.example.mymp3player.settings.ColorPickerDialog
import com.example.mymp3player.settings.ColorSettingsPage
import com.example.mymp3player.settings.SettingsMainPage
import com.example.mymp3player.settings.SettingsPage
import com.example.mymp3player.themes.AllInitialThemes
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.TemplateSettingsPage
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerSettingsOverlay(
    state: PlayerState,
    pendingTheme: AppTheme,
    page: SettingsPage,
    selectedPlaylistName: String?,
    editingColorKey: String?,
    playlistsFlow: StateFlow<List<Playlist>>,
    playlistController: PlaylistController,
    uiController: UIController,
    settingsPanelBackground: Color,
    settingsPanelText: Color,
    settingsPanelBorder: Color,
    isPlaylistSelected: Boolean,
    onPageChange: (SettingsPage) -> Unit,
    onSelectedPlaylistChange: (String?) -> Unit,
    onClose: () -> Unit,
    onMusicFolderClick: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onPreviewTemplate: (AppTheme) -> Unit,
    onEditColor: (String?) -> Unit,
    onColorChanged: (String, Color) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit
) {
    val playlists by playlistsFlow.collectAsState()
    val playlistGroups by playlistController.playlistGroups.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable {
                    onClose()
                }
        )

        // settings panel
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(
                    if (page == SettingsPage.TEMPLATES) 0.62f else 0.85f
                )
                .wrapContentHeight()
                .background(
                    settingsPanelBackground,
                    RoundedCornerShape(20.dp)
                )
                .border(
                    3.dp,
                    settingsPanelBorder,
                    RoundedCornerShape(20.dp)
                )
                .clickable {}
                .padding(
                    if (page == SettingsPage.TEMPLATES) 12.dp else 20.dp
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (page == SettingsPage.MAIN) {
                                onClose()
                            } else {
                                onPageChange(SettingsPage.MAIN)
                                onSelectedPlaylistChange(null)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = settingsPanelText
                        )
                    }

                    Text(
                        text = when (page) {
                            SettingsPage.MAIN -> "Settings"
                            SettingsPage.COLORS -> "Colors"
                            SettingsPage.TEMPLATES -> "Templates"
                            SettingsPage.PLAYLISTS -> "Playlists"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.Center),
                        color = settingsPanelText
                    )
                }

                when (page) {
                    // MAIN
                    SettingsPage.MAIN -> {
                        SettingsMainPage(
                            playInBackground = state.playInBackground,
                            onPlayInBackgroundChange = { enabled ->
                                uiController.setPlayInBackground(enabled)
                            },
                            pauseOnHeadphoneDisconnect =
                                state.pauseOnHeadphoneDisconnect,
                            onPauseOnHeadphoneDisconnectChange = { enabled ->
                                uiController.setPauseOnHeadphoneDisconnect(enabled)
                            },
                            animateSongTitle = state.animateSongTitle,
                            onAnimateSongTitleChange = { enabled ->
                                uiController.setAnimateSongTitle(enabled)
                            },
                            onMusicFolderClick = {
                                onMusicFolderClick()
                            },
                            onTemplatesClick = {
                                onPageChange(SettingsPage.TEMPLATES)
                            },
                            onPlaylistsClick = {
                                onPageChange(SettingsPage.PLAYLISTS)
                            },
                            isPlaylistActive = isPlaylistSelected,
                            keepScreenOn = state.keepScreenOn,
                            onKeepScreenOnChange = { enabled ->
                                uiController.setKeepScreenOn(enabled)
                            },
                        )
                    }

                    // COLORS
                    SettingsPage.COLORS -> {
                        ColorSettingsPage(
                            theme = pendingTheme,
                            onEditColor = { key ->
                                onEditColor(key)
                            }
                        )
                    }

                    // TEMPLATES
                    SettingsPage.TEMPLATES -> {
                        TemplateSettingsPage(
                            templates = AllInitialThemes,
                            lightingStates = state.themeAnimationsEnabled,
                            onSelectTemplate = { selectedTheme ->
                                onThemeSelected(selectedTheme)
                            },
                            onLighting = { themeName, enabled ->
                                uiController.setThemeAnimationEnabled(
                                    themeName = themeName,
                                    enabled = enabled
                                )
                            },
                            currentThemeName = AllInitialThemes
                                .firstOrNull { it.theme == state.theme }
                                ?.name
                                ?: "Original",
                        )
                    }

                    // PLAYLISTS
                    SettingsPage.PLAYLISTS -> {
                        PlaylistSettingsPage(
                            playlists = playlists.sortedBy { it.name.lowercase() },
                            playlistGroups = playlistGroups,
                            allSongs = state.allSongs,
                            selectedPlaylistName = selectedPlaylistName,

                            onSelectPlaylist = { name ->
                                onSelectedPlaylistChange(name)
                            },

                            onCreatePlaylist = { name ->
                                playlistController.createPlaylist(name)
                            },

                            onDeletePlaylist = { name ->
                                playlistController.deletePlaylist(name)

                                if (selectedPlaylistName == name) {
                                    onSelectedPlaylistChange(null)
                                }
                            },

                            onAddSong = { playlistName, song ->
                                playlistController.addSong(playlistName, song)
                            },

                            onRemoveSong = { playlistName, song ->
                                playlistController.removeSong(playlistName, song)
                            },

                            onAddSongs = { playlistName, songs ->
                                playlistController.addSongs(playlistName, songs)
                            },
                            onRemoveSongs = { playlistName, songs ->
                                playlistController.removeSongs(playlistName, songs)
                            },

                            onPlayPlaylist = { playlist ->
                                onPlayPlaylist(playlist)
                            },

                            onCreatePlaylistGroup = { name ->
                                playlistController.createPlaylistGroup(name)
                            },

                            onDeletePlaylistGroup = { name ->
                                playlistController.deletePlaylistGroup(name)
                            },

                            onAddPlaylistToGroup = { groupName, playlistName ->
                                playlistController.addPlaylistToGroup(groupName, playlistName)
                            },

                            onRemovePlaylistFromGroup = { groupName, playlistName ->
                                playlistController.removePlaylistFromGroup(groupName, playlistName)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(20.dp))

                if (page != SettingsPage.TEMPLATES) {
                    Text(
                        text = "Klivorion",
                        color = Color(0xFFFF9800),
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme
                            .typography
                            .titleLarge
                            .fontSize
                    )
                }
            }
        }
    }

    // Color picker
    editingColorKey?.let { key ->
        ColorPickerDialog(
            initialColor = pendingTheme.readColor(key),
            onDismiss = {
                onEditColor(null)
            },
            onApply = { color ->
                onColorChanged(key, color)
            }
        )
    }
}

private fun AppTheme.readColor(key: String): Color {
    return when (key) {
        "screenBackground" -> screenBackground
        "headerBackground" -> headerBackground
        "headerBorder" -> headerBorder
        "headerTitleText" -> headerTitleText
        "headerSongText" -> headerSongText
        "settingsIcon" -> settingsIcon
        "sourceButtonColor" -> sourceButtonColor
        "settingsButtonColor" -> settingsButtonColor
        "settingsBackground" -> settingsBackground
        "settingsText" -> settingsText
        "settingsSecondaryText" -> settingsSecondaryText
        "listBackground" -> listBackground
        "listBorder" -> listBorder
        "listText" -> listText
        "activeSongColor" -> activeSongColor
        "allSongsColor" -> allSongsColor
        "folderColor" -> folderColor
        "playlistColor" -> playlistColor
        "sourceIcon" -> sourceIcon
        "randomActive" -> randomActive
        "randomInactive" -> randomInactive
        "scrollTrack" -> scrollTrack
        "scrollThumb" -> scrollThumb
        "songBarTrack" -> songBarTrack
        "songBarProgress" -> songBarProgress
        "songBarThumb" -> songBarThumb
        "songTimeText" -> songTimeText
        "prevBg" -> prevBg
        "prevBorder" -> prevBorder
        "prevIcon" -> prevIcon
        "playBg" -> playBg
        "playBorder" -> playBorder
        "playIcon" -> playIcon
        "nextBg" -> nextBg
        "nextBorder" -> nextBorder
        "nextIcon" -> nextIcon
        else -> Color.Transparent
    }
}