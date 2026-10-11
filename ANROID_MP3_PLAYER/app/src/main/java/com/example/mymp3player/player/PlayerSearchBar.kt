package com.example.mymp3player.player

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymp3player.themes.AppTheme

@Composable
fun PlayerSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    theme: AppTheme
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp,vertical = 4.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Αναζήτηση",
                tint = theme.settingsIcon
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Καθαρισμός",
                        tint = theme.settingsIcon
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "Αναζήτηση τραγουδιού...",
                color = theme.settingsSecondaryText
            )
        },
        shape = RoundedCornerShape(12.dp)
    )
}