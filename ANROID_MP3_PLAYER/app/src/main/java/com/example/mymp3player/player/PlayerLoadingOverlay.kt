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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import com.example.mymp3player.themes.AppTheme

@Composable
fun PlayerLoadingOverlay(
    theme: AppTheme,
    loadingSongsCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.60f)
            )
            .clickable {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .background(
                    theme.listBackground,
                    RoundedCornerShape(22.dp)
                )
                .border(
                    2.dp,
                    theme.listBorder,
                    RoundedCornerShape(22.dp)
                )
                .padding(
                    horizontal = 28.dp,
                    vertical = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(58.dp),
                color = theme.scrollThumb,
                strokeWidth = 5.dp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text =
                    if (loadingSongsCount > 0) {
                        "$loadingSongsCount Songs Loaded"
                    } else {
                        "Loading Songs..."
                    },
                color = theme.listText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

        }
    }
}