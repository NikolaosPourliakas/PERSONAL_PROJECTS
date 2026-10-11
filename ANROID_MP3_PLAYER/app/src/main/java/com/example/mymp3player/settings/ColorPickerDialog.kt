package com.example.mymp3player.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onApply: (Color) -> Unit
) {
    val settingsBackground = Color(0xFFF5F5F5)
    val settingsText = Color(0xFF202124)
    val settingsSecondaryText = Color(0xFF5F6368)
    val settingsBorder = Color(0xFFBDBDBD)
    val settingsButton = Color(0xFF546E7A)

    var selectedColor by remember(initialColor) {
        mutableStateOf(initialColor)
    }
    var hexValue by remember(initialColor) {
        mutableStateOf(colorToHexInternal(initialColor))
    }
    val hsvValues = remember(initialColor) {
        colorToHsvInternal(initialColor)
    }
    var hue by remember(initialColor) {
        mutableFloatStateOf(hsvValues[0])
    }
    var saturation by remember(initialColor) {
        mutableFloatStateOf(hsvValues[1])
    }
    var value by remember(initialColor) {
        mutableFloatStateOf(hsvValues[2])
    }

    fun updateFromHsv(newHue: Float,newSaturation: Float,newValue: Float) {
        hue = newHue.coerceIn(0f, 360f)
        saturation = newSaturation.coerceIn(0f, 1f)
        value = newValue.coerceIn(0f, 1f)
        selectedColor = Color.hsv(hue,saturation,value)
        hexValue = colorToHexInternal(selectedColor)
    }

    fun updateFromHex(text: String) {
        hexValue = text

        val parsed = colorFromHexInternal(text)

        if (parsed != null) {
            selectedColor = parsed
            val hsv = colorToHsvInternal(parsed)
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = settingsBackground,

        title = {
            Text(text = "Choose Color",color = settingsText)
        },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                val x = offset.x.coerceIn(0f,width)
                                val y = offset.y.coerceIn(0f,height)
                                val newSaturation = (x / width).coerceIn(0f, 1f)
                                val newValue = (1f - y / height).coerceIn(0f, 1f)
                                updateFromHsv(hue,newSaturation,newValue)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                val x = change.position.x.coerceIn(0f,width)
                                val y = change.position.y.coerceIn(0f,height)
                                val newSaturation = (x / width).coerceIn(0f, 1f)
                                val newValue = (1f - y / height).coerceIn(0f, 1f)
                                updateFromHsv(hue,newSaturation,newValue)
                                change.consume()
                            }
                        }
                ) {
                    SaturationValuePalette(
                        hue = hue,
                        saturation = saturation,
                        value = value,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                HueSlider(
                    hue = hue,
                    onHueChanged = {
                        updateFromHsv(
                            it,
                            saturation,
                            value
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(selectedColor,CircleShape)
                            .border(2.dp,settingsBorder,CircleShape)
                    )

                    OutlinedTextField(
                        value = hexValue,
                        onValueChange = {
                            updateFromHex(it)
                        },
                        label = {
                            Text("HEX")
                        },
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedTextColor = settingsText,
                                unfocusedTextColor = settingsText,
                                focusedBorderColor = settingsButton,
                                unfocusedBorderColor = settingsBorder
                            )
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onApply(selectedColor)
                },
                colors = ButtonDefaults.textButtonColors(contentColor = settingsButton)
            ) {
                Text("OK")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = settingsSecondaryText)
            ) {
                Text("Cancel")
            }
        }
    )
}

/* ----- Color -> HEX ----- */

private fun colorToHexInternal(color: Color): String {
    val red = (color.red * 255f).roundToIntSafe()
    val green = (color.green * 255f).roundToIntSafe()
    val blue = (color.blue * 255f).roundToIntSafe()
    return String.format("#%02X%02X%02X",red,green,blue)
}

private fun Float.roundToIntSafe(): Int {
    return (this + 0.5f).toInt().coerceIn(0, 255)
}

/* ----- HEX -> Color ----- */

private fun colorFromHexInternal(text: String): Color? {

    val clean = text.trim().removePrefix("#")

    return try {
        when (clean.length) {
            6 -> {
                val rgb = clean.toLong(16)
                Color(
                    red = ((rgb shr 16) and 0xFF) / 255f,
                    green = ((rgb shr 8) and 0xFF) / 255f,
                    blue = (rgb and 0xFF) / 255f,
                    alpha = 1f
                )
            }
            8 -> {
                val argb = clean.toLong(16)
                Color(
                    red = ((argb shr 16) and 0xFF) / 255f,
                    green = ((argb shr 8) and 0xFF) / 255f,
                    blue = (argb and 0xFF) / 255f,
                    alpha = ((argb shr 24) and 0xFF) / 255f
                )
            }
            else -> null
        }
    } catch (_: NumberFormatException) {
        null
    }
}

/* ----- HSV conversion ----- */

private fun colorToHsvInternal(color: Color): FloatArray {

    val r = color.red
    val g = color.green
    val b = color.blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var hue = 0f

    if (delta != 0f) {
        hue = when (max) {
            r -> 60f * (((g - b) / delta) % 6f)
            g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        if (hue < 0f) {
            hue += 360f
        }
    }

    val saturation = if (max == 0f) {
            0f
        } else {
            delta / max
        }

    return floatArrayOf(hue,saturation, max)
}

/* ----- Saturation / Value palette ----- */

@Composable
private fun SaturationValuePalette(
    hue: Float,
    saturation: Float,
    value: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val hueColor = Color.hsv(hue,1f,1f)
        drawRect(brush = Brush.horizontalGradient(colors = listOf(Color.White,hueColor)))
        drawRect(brush = Brush.verticalGradient(colors = listOf(Color.Transparent,Color.Black)))
        val x = saturation * size.width
        val y = (1f - value) * size.height
        drawCircle(color = Color.White,radius = 9f,center = Offset(x, y),style = Stroke(width = 3f))
        drawCircle(color = Color.Black,radius = 9f,center = Offset(x, y), style = Stroke(width = 1f))
    }
}

/* ----- Hue slider ----- */

@Composable
private fun HueSlider(hue: Float, onHueChanged: (Float) -> Unit) {
    Canvas(modifier = Modifier.fillMaxWidth().height(28.dp).pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newHue = (offset.x / size.width.toFloat() * 360f).coerceIn(0f,360f)
                    onHueChanged(newHue)
                }
            }
    ) {

        val hueColors = listOf(
            Color.Red,Color.Yellow,Color.Green,Color.Cyan,Color.Blue,Color.Magenta,Color.Red)

        drawRoundRect(brush = Brush.horizontalGradient(colors = hueColors))

        val x = (hue / 360f).coerceIn(0f, 1f) * size.width

        drawCircle(
            color = Color.White,
            radius = 9f,
            center = Offset(x,size.height / 2f),
            style = Stroke(width = 3f)
        )

        drawCircle(
            color = Color.Black,
            radius = 9f,
            center = Offset(x,size.height / 2f),
            style = Stroke(width = 1f)
        )
    }
}