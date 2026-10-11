package com.example.mymp3player.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height

@Composable
fun TemplateSettingsPage(
    templates: List<ThemeItem>,
    lightingStates: Set<String>,
    currentThemeName: String,
    onSelectTemplate: (AppTheme) -> Unit,
    onLighting: (String, Boolean) -> Unit
) {
    val settingsText = Color(0xFF202124)
    val settingsActive = Color(0xFF546E7A)
    val lightingActiveColor = Color(0xFFFFC107)

    val orderedNames = listOf(
        "Original",
        "Mono",
        "Pinball",
        "Disco",
        "Cyber",
        "Concert"
    )

    val visibleTemplates = remember(templates) {
        orderedNames.mapNotNull { name ->
            templates.firstOrNull { it.name == name }
        }
    }

    val selectedTemplateName = currentThemeName

    Column(modifier = Modifier.fillMaxWidth()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gridItems(items = visibleTemplates,key = { it.name }
            ) { template ->
                val isSelected = selectedTemplateName == template.name
                val lightingEnabled = template.name in lightingStates

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.4f)
                        .background(
                            color = if (isSelected) {
                                Color(0xFFECEFF1)
                            } else {
                                Color(0xFFF7F7F7)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                settingsActive
                            } else {
                                Color(0xFFD5D5D5)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onSelectTemplate(template.theme)
                        }
                        .padding(10.dp)
                ) {
                    // Όνομα theme και φωτισμός.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = template.name,
                            color = settingsText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(
                            onClick = {
                                onLighting(template.name,!lightingEnabled)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Lighting ${template.name}",
                                tint = if (lightingEnabled) {
                                    lightingActiveColor
                                } else {
                                    settingsText
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                color = template.theme.headerBackground,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Κύριο χρώμα του theme
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(
                                        color = template.theme.headerBorder,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )

                            // Δύο δευτερεύοντα χρώματα
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .background(
                                            color = template.theme.playlistColor,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .background(
                                            color = template.theme.randomActive,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun ColorPreview(color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 3.dp)
            .size(18.dp)
            .background(color = color,shape = CircleShape)
            .border(width = 1.dp,color = Color.Gray,shape = CircleShape)
    )
}