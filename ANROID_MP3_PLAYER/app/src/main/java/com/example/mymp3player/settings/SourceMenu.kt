package com.example.mymp3player.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymp3player.Song
import com.example.mymp3player.themes.AppTheme

private data class FolderNode(
    val name: String,
    val path: String,
    val songsCount: Int,
    val children: List<FolderNode>
)

private fun countSongsInFolder(allSongs: List<Song>, folderPath: String): Int {
    return allSongs.count { song ->
        val path = song.folderName ?: return@count false
        path == folderPath || path.startsWith("$folderPath /")
    }
}

@Composable
private fun FolderTreeItem(
    node: FolderNode,
    allSongs: List<Song>,
    currentSourceName: String,
    theme: AppTheme,
    depth: Int,
    expandedPaths: Set<String>,
    onToggleExpanded: (String) -> Unit,
    onFolderClick: (String) -> Unit
) {
    val folderColor = theme.folderColor
    val isRoot = depth == 0
    val isExpanded = node.path in expandedPaths
    val isSelected = currentSourceName == "Folder: ${node.path}"
    val textColor =
        if (isSelected) {
            folderColor
        } else {
            theme.listText
        }
    val totalSongs =
        remember(allSongs, node.path) {
            countSongsInFolder(allSongs = allSongs,folderPath = node.path)
        }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color =
                        when {
                            isSelected -> folderColor.copy(alpha = 0.12f)
                            isRoot -> folderColor.copy(alpha = 0.07f)
                            else -> Color.Transparent
                        },
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(
                    start = (14 + depth * 20).dp,
                    end = 14.dp,
                    top = if (isRoot) 10.dp else 8.dp,
                    bottom = if (isRoot) 10.dp else 8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically) {
            if (node.children.isNotEmpty()) {
                Text(
                    text =
                        if (isExpanded) {
                            "▲"
                        } else {
                            "▼"
                        },
                    color = folderColor,
                    modifier = Modifier.size(28.dp).clickable {
                            onToggleExpanded(node.path)
                        }
                )
            } else {
                Spacer(Modifier.width(28.dp))
            }

            Spacer(Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = folderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onFolderClick(node.path)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =
                        if (node.children.isNotEmpty() && isExpanded) {
                            Icons.Default.FolderOpen
                        } else {
                            Icons.Default.Folder
                        },
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onFolderClick(node.path)
                    }
            ) {
                Text(
                    text = node.name,
                    color = textColor,
                    fontWeight =
                        if (isRoot || isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$totalSongs songs",
                    color = textColor.copy(alpha = 0.60f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (isExpanded) {
            node.children.forEach { child ->
                FolderTreeItem(
                    node = child,
                    allSongs = allSongs,
                    currentSourceName = currentSourceName,
                    theme = theme,
                    depth = depth + 1,
                    expandedPaths = expandedPaths,
                    onToggleExpanded = onToggleExpanded,
                    onFolderClick = onFolderClick
                )
            }
        }
    }
}

