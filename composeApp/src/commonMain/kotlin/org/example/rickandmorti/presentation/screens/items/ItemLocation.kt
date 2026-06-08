package org.example.rickandmorti.presentation.screens.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import org.example.rickandmorti.domain.model.Location

@Composable
fun ItemLocation(
    location: Location,
    isFavorite: Boolean,
    onToggleFavorite: (Location) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = location.name, style = MaterialTheme.typography.titleMedium)
                Text(text = location.type, style = MaterialTheme.typography.bodySmall)
                Text(text = location.dimension, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = { onToggleFavorite(location) }) {
                Icon(
                    imageVector = if (isFavorite) TablerIcons.Heart else TablerIcons.Heart,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else LocalContentColor.current.copy(alpha = 0.5f)
                )
            }
        }
    }
}