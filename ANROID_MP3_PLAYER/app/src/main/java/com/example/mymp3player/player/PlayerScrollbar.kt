
package com.example.mymp3player.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun PlayerScrollbar(
    totalItems: Int,
    visibleItems: Int,
    firstVisibleItemIndex: Int,
    currentItemIndex: Int,
    onScrollToIndex: suspend (Int) -> Unit,
    trackColor: Color,
    thumbColor: Color
) {
    if (totalItems <= 0) return

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val markerSizePx = with(density) { 14.dp.toPx() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .width(24.dp)
    ) {
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        val safeVisibleItems = visibleItems
            .coerceAtLeast(1)
            .coerceAtMost(totalItems)

        val maxFirstIndex = (totalItems - safeVisibleItems)
            .coerceAtLeast(0)

        val thumbFraction = (safeVisibleItems.toFloat() / totalItems)
            .coerceIn(0.05f, 1f)

        val thumbHeightPx = (heightPx * thumbFraction)
            .coerceIn(markerSizePx.coerceAtMost(heightPx), heightPx)

        val travelHeightPx = (heightPx - thumbHeightPx)
            .coerceAtLeast(0f)

        val scrollFraction = if (maxFirstIndex == 0) {
            0f
        } else {
            (firstVisibleItemIndex.toFloat() / maxFirstIndex)
                .coerceIn(0f, 1f)
        }

        val thumbY = scrollFraction * travelHeightPx

        val dragThumbY = remember { mutableFloatStateOf(thumbY) }
        val isDragging = remember { androidx.compose.runtime.mutableStateOf(false) }

        LaunchedEffect(thumbY, isDragging.value) {
            if (!isDragging.value) {
                dragThumbY.floatValue = thumbY
            }
        }

        val displayedThumbY = dragThumbY.floatValue
            .coerceIn(0f, travelHeightPx)

        val currentFraction = if (
            currentItemIndex < 0 || totalItems <= 1
        ) {
            0f
        } else {
            currentItemIndex.coerceIn(0, totalItems - 1).toFloat() /
                    (totalItems - 1)
        }

        val currentY = (currentFraction * (heightPx - markerSizePx))
            .coerceIn(0f, (heightPx - markerSizePx).coerceAtLeast(0f))

        fun indexFromY(y: Float): Int {
            if (maxFirstIndex == 0 || travelHeightPx <= 0f) return 0

            return ((y / travelHeightPx).coerceIn(0f, 1f) * maxFirstIndex)
                .roundToInt()
                .coerceIn(0, maxFirstIndex)
        }

        fun scrollToY(y: Float) {
            val safeY = y.coerceIn(0f, travelHeightPx)
            dragThumbY.floatValue = safeY
            scope.launch {
                onScrollToIndex(indexFromY(safeY))
            }
        }

        Box(modifier = Modifier.fillMaxHeight().width(24.dp)) {
            // Track
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .background(trackColor, RoundedCornerShape(2.dp))
            )

            if (currentItemIndex >= 0) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(0, currentY.roundToInt())
                        }
                        .align(Alignment.TopCenter)
                        .size(14.dp)
                        .background(
                            thumbColor.copy(alpha = 0.30f),
                            CircleShape
                        )
                        .pointerInput(currentItemIndex, totalItems) {
                            detectTapGestures(
                                onTap = {
                                    if (currentItemIndex >= 0) {
                                        scope.launch {
                                            onScrollToIndex(
                                                currentItemIndex.coerceIn(0, maxFirstIndex)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(24.dp)
                    .pointerInput(totalItems, visibleItems, heightPx) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitPointerEvent()
                                    .changes
                                    .firstOrNull { it.pressed }
                                    ?: continue

                                val nearCurrentSong =
                                    currentItemIndex >= 0 &&
                                            kotlin.math.abs(down.position.y - currentY) <= markerSizePx * 2f

                                if (nearCurrentSong) {
                                    val startY = down.position.y
                                    var moved = false

                                    down.consume()

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val pointer = event.changes.firstOrNull() ?: break

                                        if (!pointer.pressed) {
                                            if (!moved) {
                                                scope.launch {
                                                    onScrollToIndex(
                                                        currentItemIndex.coerceIn(0, maxFirstIndex)
                                                    )
                                                }
                                            }
                                            break
                                        }

                                        if (kotlin.math.abs(pointer.position.y - startY) > markerSizePx) {
                                            moved = true
                                            break
                                        }

                                        pointer.consume()
                                    }

                                    if (!moved) continue
                                }

                                val targetY = (down.position.y - thumbHeightPx / 2f)
                                    .coerceIn(0f, travelHeightPx)

                                dragThumbY.floatValue = targetY

                                scope.launch {
                                    onScrollToIndex(indexFromY(targetY))
                                }

                                down.consume()

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break

                                    if (!pointer.pressed) break

                                    val newY = (pointer.position.y - thumbHeightPx / 2f)
                                        .coerceIn(0f, travelHeightPx)

                                    dragThumbY.floatValue = newY

                                    scope.launch {
                                        onScrollToIndex(indexFromY(newY))
                                    }

                                    pointer.consume()
                                }
                            }
                        }
                    }
            )

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(0, displayedThumbY.roundToInt())
                    }
                    .align(Alignment.TopCenter)
                    .size(14.dp)
                    .background(thumbColor, CircleShape)
                    .pointerInput(totalItems, visibleItems, travelHeightPx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging.value = true

                                dragThumbY.floatValue =
                                    (displayedThumbY + offset.y - markerSizePx / 2f)
                                        .coerceIn(0f, travelHeightPx)

                                scrollToY(dragThumbY.floatValue)
                            },
                            onDragEnd = {
                                isDragging.value = false
                            },
                            onDragCancel = {
                                isDragging.value = false
                            }
                        ) { change, dragAmount ->
                            if (maxFirstIndex == 0 || travelHeightPx <= 0f) {
                                return@detectDragGestures
                            }

                            change.consume()

                            val newY = (dragThumbY.floatValue + dragAmount.y)
                                .coerceIn(0f, travelHeightPx)

                            dragThumbY.floatValue = newY

                            scope.launch {
                                onScrollToIndex(indexFromY(newY))
                            }
                        }
                    }
            )
        }
    }
}
