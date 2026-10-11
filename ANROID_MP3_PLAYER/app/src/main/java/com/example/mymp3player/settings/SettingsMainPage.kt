package com.example.mymp3player.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size

@Composable
fun SettingsMainPage(
    playInBackground: Boolean,
    pauseOnHeadphoneDisconnect: Boolean,
    animateSongTitle: Boolean,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onPlayInBackgroundChange: (Boolean) -> Unit,
    onPauseOnHeadphoneDisconnectChange: (Boolean) -> Unit,
    onAnimateSongTitleChange: (Boolean) -> Unit,
    onMusicFolderClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    isPlaylistActive: Boolean
) {
    val settingsText = Color(0xFF202124)
    val settingsSecondaryText = Color(0xFF5F6368)
    val settingsActive = Color(0xFF546E7A)
    val settingsActiveBackground = Color(0xFFECEFF1)

    Column {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "📁 Music Folder",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onMusicFolderClick()
                }
                .padding(16.dp),
            fontSize = 18.sp,
            color = settingsText
        )

        Text(
            text = "📝 Playlists",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onPlaylistsClick()
                }
                .background(
                    if (isPlaylistActive) {
                        settingsActiveBackground
                    } else {
                        Color.Transparent
                    }
                )
                .padding(16.dp),
            fontSize = 18.sp,
            color = if (isPlaylistActive) {
                settingsActive
            } else {
                settingsText
            },
            fontWeight = if (isPlaylistActive) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )

        Text(
            text = "🎨 Color Templates",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onTemplatesClick()
                }
                .padding(16.dp),
            fontSize = 18.sp,
            color = settingsText
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onPlayInBackgroundChange(!playInBackground)
                }
                .padding(horizontal = 12.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = playInBackground,
                onCheckedChange = onPlayInBackgroundChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = settingsActive,
                    uncheckedColor = settingsSecondaryText,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Play In Background",
                color = settingsText,
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onKeepScreenOnChange(!keepScreenOn) }
                .padding(horizontal = 12.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = settingsActive,
                    uncheckedColor = settingsSecondaryText,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Keep Screen On",
                color = settingsText,
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onPauseOnHeadphoneDisconnectChange(!pauseOnHeadphoneDisconnect)
                }
                .padding(horizontal = 12.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = pauseOnHeadphoneDisconnect,
                onCheckedChange = onPauseOnHeadphoneDisconnectChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = settingsActive,
                    uncheckedColor = settingsSecondaryText,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Unplugged Headphones Pause",
                color = settingsText,
                fontSize = 16.sp
            )
        }
    }
}