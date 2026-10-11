package com.example.mymp3player.player

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymp3player.PlayerState
import com.example.mymp3player.Song
import com.example.mymp3player.themes.AppTheme

@Composable
fun PlayerPreview(
    state: PlayerState,
    currentSong: Song?,
    theme: AppTheme,
    currentSourceName: String,
    isAllSongsSelected: Boolean,
    isFolderSelected: Boolean,
    isPlaylistSelected: Boolean,
    allSongsColor: Color,
    folderColor: Color,
    playlistColor: Color,
    filteredSongs: List<Song>,
    currentSongAssetPath: String?,
    filteredCurrentIndex: Int,
    listState: LazyListState,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.screenBackground)
            .clickable {
                onClose()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            PlayerHeader(
                currentSong = currentSong,
                theme = theme,
                onSettingsClick = { onClose() },
                animateSongTitle = state.animateSongTitle,
                isRandom = state.isRandom,
                onShuffleClick = {},
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    tint = if (state.isRandom) {
                        theme.randomActive
                    } else {
                        theme.randomInactive
                    }
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = theme.listText
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
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
                        onSongClick = {
                            onClose()
                        }
                    )
                }

                val visibleItems =
                    listState
                        .layoutInfo
                        .visibleItemsInfo
                        .size

                PlayerScrollbar(
                    totalItems = filteredSongs.size,
                    visibleItems = visibleItems,
                    firstVisibleItemIndex =
                        listState.firstVisibleItemIndex,
                    currentItemIndex =
                        filteredCurrentIndex,
                    onScrollToIndex = {
                        onClose()
                    },
                    trackColor = theme.scrollTrack,
                    thumbColor = theme.scrollThumb
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            SongProgressBar(
                position = state.currentPosition,
                duration = state.duration,
                onSeek = {
                    onClose()
                },
                trackColor = theme.scrollTrack,
                thumbColor = theme.scrollThumb,
                timeTextColor = theme.songTimeText
            )

            Spacer(
                Modifier.height(14.dp)
            )

            PlayerControls(
                isPlaying = state.isPlaying,
                theme = theme,
                onPrevious = {
                    onClose()
                },
                onPlayPause = {
                    onClose()
                },
                onNext = {
                    onClose()
                }
            )
        }
    }
}