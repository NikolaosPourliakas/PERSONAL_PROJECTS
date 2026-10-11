package com.example.mymp3player.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorRow(key: String,color: Color, onClick: (String) -> Unit) {
    val settingsText = Color(0xFF202124)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(key)
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = key,color = settingsText)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = colorToHexInternal(color),color = settingsText)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(24.dp).background(color))
        }
    }
}

private fun colorToHexInternal(color: Color): String {
    val red = (color.red * 255f + 0.5f).toInt().coerceIn(0, 255)
    val green = (color.green * 255f + 0.5f).toInt().coerceIn(0, 255)
    val blue = (color.blue * 255f + 0.5f).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", red, green, blue)
}