package org.example.rickandmorti.presentation.screens.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import org.example.rickandmorti.domain.model.Character

@Composable
fun ItemCharacter(
    character: Character,
    isFavorite: Boolean,
    onToggleFavorite: (Character) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column (
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = character.image,
                contentDescription = null,
                modifier = Modifier.size(128.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = character.status, style = MaterialTheme.typography.bodySmall)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            color = when(character.status) {
                                "Alive" -> Color.Green
                                "Dead" -> Color.Red
                                else -> Color.Gray
                            })
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(0.8f),
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                    )

                IconButton(
                    modifier = Modifier.wrapContentWidth().weight(0.2f),
                    onClick = { onToggleFavorite(character) }
                ) {
                    Icon(
                        imageVector = if (isFavorite) TablerIcons.Heart else TablerIcons.Heart,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else LocalContentColor.current.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}