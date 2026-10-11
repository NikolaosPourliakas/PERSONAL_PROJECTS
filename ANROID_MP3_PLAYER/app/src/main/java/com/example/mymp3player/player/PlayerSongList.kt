package com.example.mymp3player.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymp3player.Song
import com.example.mymp3player.themes.AppTheme

@Composable
fun PlayerSongList(
    songs: List<Song>,
    allDisplayedSongs: List<Song>,
    currentSongUri: String?,
    theme: AppTheme,
    listState: LazyListState,
    onSongClick: (Int) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(4.dp)
    ) {
        items(
            items = songs,
            key = {
                song -> song.assetPath
            }
        ) {
            song ->
            val originalIndex = allDisplayedSongs.indexOfFirst {
                    it.assetPath == song.assetPath
                }
            val isCurrent =
                song.assetPath == currentSongUri
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (originalIndex >= 0) {
                            onSongClick(originalIndex)
                        }
                    }
                    .background(
                        color =
                            if (isCurrent) {
                                theme.activeSongColor.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp,vertical = 10.dp
                    )
            ) {
                Text(
                    text = song.title,
                    color =
                        if (isCurrent) {
                            theme.activeSongColor
                        } else {
                            theme.listText
                        },
                    fontWeight =
                        if (isCurrent) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}