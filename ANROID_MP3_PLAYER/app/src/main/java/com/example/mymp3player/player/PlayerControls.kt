package com.example.mymp3player.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.themes_setup.CyberTargets

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    theme: AppTheme,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        /* PREVIOUS */

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(64.dp)

                    .onGloballyPositioned { coordinates ->
                        CyberTargets.setTarget(
                            id = CyberTargets.PREVIOUS,
                            rect = coordinates.boundsInRoot()
                        )
                    }
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = theme.prevBorder,
                        shape = CircleShape
                    )
                    .background(
                        color = theme.prevBg,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = theme.prevIcon,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        /* PLAY / PAUSE */

        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(72.dp)
                .onGloballyPositioned { coordinates ->
                    CyberTargets.setTarget(
                        id = CyberTargets.PLAY,
                        rect = coordinates.boundsInRoot()
                    )
                }
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = theme.playBorder,
                    shape = CircleShape
                )
                .background(
                    color = theme.playBg,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (isPlaying) {
                    "Pause"
                } else {
                    "Play"
                },
                tint = theme.playIcon,
                modifier = Modifier.size(52.dp)
            )
        }

        /* NEXT */

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(64.dp)
                .onGloballyPositioned { coordinates ->
                    CyberTargets.setTarget(
                        id = CyberTargets.NEXT,
                        rect = coordinates.boundsInRoot()
                    )
                }
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = theme.nextBorder,
                    shape = CircleShape
                )
                .background(
                    color = theme.nextBg,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = theme.nextIcon,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}