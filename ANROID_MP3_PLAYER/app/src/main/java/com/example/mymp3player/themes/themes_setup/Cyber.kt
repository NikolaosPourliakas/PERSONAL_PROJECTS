
package com.example.mymp3player.themes.themes_setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.mymp3player.UIController
import com.example.mymp3player.themes.AppTheme
import com.example.mymp3player.themes.ThemeAnimation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

object CyberTargets {
    const val HEADER = "cyber_header"
    const val LIST = "cyber_list"
    const val PROGRESS_BAR = "cyber_progress_bar"
    const val PREVIOUS = "cyber_previous"
    const val PLAY = "cyber_play"
    const val NEXT = "cyber_next"

    private val targets = mutableStateMapOf<String, Rect>()

    fun setTarget(id: String, rect: Rect) {
        targets[id] = rect
    }

    fun getTargets(): Map<String, Rect> = targets.toMap()
}

private data class CyberNode(
    val col: Int,
    val row: Int
)

private class CyberPulseState(val seed: Int) {
    val progress = Animatable(0f)
    var route by mutableStateOf<List<CyberNode>>(emptyList())
    var intensity by mutableFloatStateOf(0.5f)
}

object CyberAnimation : ThemeAnimation {
    override val durationMillis: Int = 9000
    override val pauseMillis: Long = 0L
    override fun nextTheme(baseTheme: AppTheme): AppTheme = baseTheme

    @Composable
    fun CircuitOverlay(uiController: UIController,modifier: Modifier = Modifier) {
        Overlay(uiController, modifier)
    }

    @Composable
    fun Overlay(uiController: UIController,modifier: Modifier = Modifier) {
        val state by uiController.state.collectAsState()
        if (state.theme.animation !== CyberAnimation || "Cyber" !in state.themeAnimationsEnabled) {
            return
        }

        val columns = 12
        val rows = 20

        val pulses = remember {
            List(28) {
                index -> CyberPulseState(seed = Random.nextInt() + index * 7919)
            }
        }

        LaunchedEffect(pulses) {
            coroutineScope {
                pulses.forEach { pulse ->
                    launch {
                        val random = Random(pulse.seed)
                        kotlinx.coroutines.delay(
                            random.nextLong(0L, 1800L).milliseconds
                        )

                        while (true) {
                            pulse.intensity = random.nextFloat().coerceIn(0.05f, 1f)
                            pulse.route = createRoute(
                                columns = columns,
                                rows = rows,
                                random = random,
                                steps = random.nextInt(18, 36)
                            )
                            pulse.progress.snapTo(0f)
                            val duration = (
                                    4200f - pulse.intensity * 3300f
                                    ).toInt().coerceIn(700, 4200)
                            pulse.progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = duration,easing = LinearEasing
                                )
                            )
                        }
                    }
                }
            }
        }

        val targets = CyberTargets.getTargets()
        val header = targets[CyberTargets.HEADER]
        val list = targets[CyberTargets.LIST]

        Canvas(modifier = modifier.fillMaxSize()) {
            val dx = size.width / columns
            val dy = size.height / rows
            val thinStroke = 1.dp.toPx()
            val pulseStroke = 1.5.dp.toPx()

            fun nodePosition(node: CyberNode): Offset = Offset(node.col * dx, node.row * dy)

            fun alphaAt(point: Offset): Float = when {
                targets[CyberTargets.PLAY]?.contains(point) == true -> 0.015f
                targets[CyberTargets.PREVIOUS]?.contains(point) == true -> 0.015f
                targets[CyberTargets.NEXT]?.contains(point) == true -> 0.015f
                list?.contains(point) == true -> 0.035f
                header?.contains(point) == true -> 0.09f
                else -> 0.17f
            }

            for (col in 0..columns) {
                for (row in 0 until rows) {
                    val start = Offset(col * dx, row * dy)
                    val end = Offset(col * dx, (row + 1) * dy)
                    val midpoint = Offset((start.x + end.x) / 2f,(start.y + end.y) / 2f)

                    drawLine(
                        color = cyberColor(col * 0.055f + row * 0.021f).copy(alpha = alphaAt(midpoint)),
                        start = start,
                        end = end,
                        strokeWidth = thinStroke
                    )
                }
            }

            for (row in 0..rows) {
                for (col in 0 until columns) {
                    val start = Offset(col * dx, row * dy)
                    val end = Offset((col + 1) * dx, row * dy)
                    val midpoint = Offset((start.x + end.x) / 2f,(start.y + end.y) / 2f)

                    drawLine(
                        color = cyberColor(row * 0.071f + col * 0.027f).copy(alpha = alphaAt(midpoint)),
                        start = start,
                        end = end,
                        strokeWidth = thinStroke
                    )
                }
            }

            for (col in 0..columns) {
                for (row in 0..rows) {
                    val point = Offset(col * dx, row * dy)

                    drawCircle(
                        color = cyberColor(col * 0.1f + row * 0.04f).copy(alpha = alphaAt(point) * 0.75f),
                        radius = 1.2.dp.toPx(),
                        center = point
                    )
                }
            }

            pulses.forEachIndexed { index, pulse ->
                val route = pulse.route
                if (route.size < 2) {
                    return@forEachIndexed
                }
                val progress = pulse.progress.value
                val intensity = pulse.intensity
                val lastEdge = route.size - 2
                val position = progress * (route.size - 1)
                val currentEdge = position.toInt().coerceIn(0, lastEdge)
                val edgeProgress = (position - currentEdge).coerceIn(0f, 1f)
                val trailLength = 5
                val firstTrailEdge = (currentEdge - trailLength + 1).coerceAtLeast(0)

                for (edge in firstTrailEdge until currentEdge) {
                    val start = nodePosition(route[edge])
                    val end = nodePosition(route[edge + 1])
                    val midpoint = Offset((start.x + end.x) / 2f,(start.y + end.y) / 2f)
                    val age = currentEdge - edge
                    val fade = (1f - age.toFloat() / trailLength).coerceIn(0f, 1f)
                    val trailColor = cyberColor(progress * 2.5f + index * 0.11f - age * 0.045f)

                    drawLine(
                        color = trailColor.copy(alpha =
                            (fade * alphaAt(midpoint) * (1.5f + intensity * 2.5f)).coerceIn(0f, 0.85f)),
                        start = start,
                        end = end,
                        strokeWidth = pulseStroke,
                        blendMode = BlendMode.Screen
                    )
                }

                val start = nodePosition(route[currentEdge])
                val end = nodePosition(route[currentEdge + 1])

                val point = Offset(
                    x = start.x + (end.x - start.x) * edgeProgress,
                    y = start.y + (end.y - start.y) * edgeProgress
                )

                val color = cyberColor(progress * 2.5f + index * 0.11f)

                val localAlpha = alphaAt(point)
                val headAlpha = (
                        localAlpha * (1.8f + intensity * 4f)
                        ).coerceIn(0f, 1f)

                drawLine(
                    color = color.copy(alpha = headAlpha),
                    start = start,
                    end = point,
                    strokeWidth = pulseStroke,
                    blendMode = BlendMode.Screen
                )

                drawCircle(
                    color = color.copy(alpha = headAlpha),
                    radius = (1.8f + intensity * 2.8f).dp.toPx(),
                    center = point,
                    blendMode = BlendMode.Screen
                )

                drawCircle(
                    color = color.copy(alpha = (localAlpha * intensity * 0.7f).coerceIn(0f, 0.22f)),
                    radius = (4f + intensity * 5f).dp.toPx(),
                    center = point,
                    blendMode = BlendMode.Screen
                )
            }

            header?.let { rect ->
                drawRect(
                    color = Color.Cyan.copy(alpha = 0.12f),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            list?.let { rect ->
                drawRect(
                    color = Color.Magenta.copy(alpha = 0.06f),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }

    private fun createRoute(columns: Int, rows: Int,random: Random,steps: Int): List<CyberNode> {
        var col = random.nextInt(columns + 1)
        var row = random.nextInt(rows + 1)
        val route = mutableListOf(CyberNode(col, row))
        var previousDirection = -1

        repeat(steps) {
            val possible = mutableListOf<Int>()

            if (col > 0) possible += 0
            if (col < columns) possible += 1
            if (row > 0) possible += 2
            if (row < rows) possible += 3

            val reverse = when (previousDirection) {
                0 -> 1
                1 -> 0
                2 -> 3
                3 -> 2
                else -> -1
            }
            val choices = possible.filter {
                it != reverse
            }.ifEmpty {
                possible
            }
            val direction = choices[random.nextInt(choices.size)]

            when (direction) {
                0 -> col--
                1 -> col++
                2 -> row--
                3 -> row++
            }

            route += CyberNode(col, row)
            previousDirection = direction
        }
        return route
    }

    private fun cyberColor(phase: Float): Color {
        val colors = listOf(
            Color(0xFF00FFFF),
            Color(0xFF008CFF),
            Color(0xFF7A00FF),
            Color(0xFFFF00C8),
            Color(0xFFFF6A00),
            Color(0xFF00FFFF)
        )

        val p = ((phase % 1f) + 1f) % 1f
        val scaled = p * (colors.size - 1)

        val index = kotlin.math.floor(scaled)
            .toInt()
            .coerceIn(0, colors.size - 2)

        return androidx.compose.ui.graphics.lerp(
            colors[index],
            colors[index + 1],
            scaled - index
        )
    }
}

val CyberTheme = AppTheme(
    screenBackground = Color(0xFF05070D),
    headerBackground = Color(0xFF0B1020),
    headerBorder = Color(0xFF00FFFF),
    headerTitleText = Color.White,
    headerSongText = Color(0xFFB8FFFF),
    settingsIcon = Color(0xFF00FFFF),
    sourceButtonColor = Color(0xFFFF00C8),
    settingsButtonColor = Color(0xFF00FFFF),
    settingsBackground = Color(0xFF0B1020),
    settingsText = Color.White,
    settingsSecondaryText = Color(0xFFB8FFFF),
    listBackground = Color(0xFF05070D),
    listBorder = Color(0xFF7A00FF),
    listText = Color.White,
    activeSongColor = Color(0xFF00FFFF),
    allSongsColor = Color(0xFF00FFFF),
    folderColor = Color(0xFF008CFF),
    playlistColor = Color(0xFFFF00C8),
    sourceIcon = Color(0xFF7A00FF),
    randomActive = Color(0xFFFF00C8),
    randomInactive = Color(0xFF607080),
    scrollTrack = Color(0xFF0B1020),
    scrollThumb = Color(0xFF00FFFF),
    songBarTrack = Color(0xFF0B1020),
    songBarProgress = Color(0xFF00FFFF),
    songBarThumb = Color(0xFFFF00C8),
    songTimeText = Color(0xFFB8FFFF),
    prevBg = Color(0xFF0B1020),
    prevBorder = Color(0xFF00FFFF),
    prevIcon = Color(0xFF00FFFF),
    playBg = Color(0xFF00FFFF),
    playBorder = Color(0xFFFF00C8),
    playIcon = Color(0xFF05070D),
    nextBg = Color(0xFF0B1020),
    nextBorder = Color(0xFF00FFFF),
    nextIcon = Color(0xFF00FFFF),
    animation = CyberAnimation
)
