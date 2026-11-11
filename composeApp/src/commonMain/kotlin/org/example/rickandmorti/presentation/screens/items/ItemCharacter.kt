package org.example.rickandmorti.presentation.screens.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.LocationInfo
import org.example.rickandmorti.domain.model.Origin

@Composable
fun ItemCharacter(
    modifier: Modifier = Modifier,
    character: Character,
    isFavorite: Boolean,
    onToggleFavorite: (Character) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1D2228)
        ),
        border = BorderStroke(
            2.dp,
            Color(0xFF2B3138)
        ),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column (
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { onToggleFavorite(character) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = TablerIcons.Heart,
                        contentDescription = "favorite icon",
                        tint = if (isFavorite) Color(0xFF76FF03) else Color(0xFFB0BEC5)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PortalAvatar(
                        imageUrl = character.image,
                        size = 96.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = character.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusChip(status = character.status)
                        InfoChip(character.species)
                    }
                }

            }
        }
    }
}

@Composable
private fun PortalAvatar(
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: Dp
) {
    Box(
        modifier = modifier.size(size).padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {

        PortalIndicator(
            modifier = Modifier.fillMaxSize(),
            isAnimated = false
        )

        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size * 0.78f)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun StatusChip(status: String) {

    val color =
        when (status) {
            "Alive" -> Color(0xFF4CAF50)
            "Dead" -> Color(0xFFF44336)
            else -> Color.Gray
        }

    Surface(
        color = color.copy(alpha = .15f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color,
                        CircleShape
                    )
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = status,
                color = color,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun InfoChip(
    text: String
) {
    Surface(
        color = Color(0xFF2B3138),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            color = Color(0xFFCFD8DC),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private val sampleCharacterAlive = Character(
    id = 1,
    name = "Rick Sanchez",
    status = "Alive",
    species = "Human",
    type = "",
    gender = "Male",
    origin = Origin("Earth (C-137)", ""),
    location = LocationInfo("Citadel of Ricks", ""),
    image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
    episode = emptyList(),
    url = "",
    created = ""
)

private val sampleCharacterDead = Character(
    id = 2,
    name = "Morty Smith",
    status = "Dead",
    species = "Human",
    type = "",
    gender = "Male",
    origin = Origin("Earth (C-137)", ""),
    location = LocationInfo("Earth (Replacement Dimension)", ""),
    image = "https://rickandmortyapi.com/api/character/avatar/2.jpeg",
    episode = emptyList(),
    url = "",
    created = ""
)

@Preview
@Composable
fun ItemCharacterStatesPreview() {
    MaterialTheme {
        Surface {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.width(180.dp)) {
                    ItemCharacter(
                        character = sampleCharacterAlive,
                        isFavorite = true,
                        onToggleFavorite = {},
                        onClick = {}
                    )
                }
                Box(modifier = Modifier.width(180.dp)) {
                    ItemCharacter(
                        character = sampleCharacterDead,
                        isFavorite = false,
                        onToggleFavorite = {},
                        onClick = {}
                    )
                }
            }
        }
    }
}
