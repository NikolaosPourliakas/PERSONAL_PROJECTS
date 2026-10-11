package com.example.mymp3player.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymp3player.Song

@Composable
fun PlaylistSongRow(
    song: Song,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val settingsText = Color(0xFF202124)
    val settingsSecondaryText = Color(0xFF5F6368)
    val settingsActive = Color(0xFF546E7A)
    val settingsSelectedBackground = Color(0xFFECEFF1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (checked) settingsSelectedBackground
                else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(0.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = settingsActive,
                uncheckedColor = settingsSecondaryText,
                checkmarkColor = Color.White
            )
        )

        Spacer(Modifier.width(2.dp))

        Text(
            text = song.title,
            modifier = Modifier
                .weight(1f)
                .padding(end = 2.dp),
            color = settingsText,
            maxLines = Int.MAX_VALUE,
            softWrap = true
        )
    }
}