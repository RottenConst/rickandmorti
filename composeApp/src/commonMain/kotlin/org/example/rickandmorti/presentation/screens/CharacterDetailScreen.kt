package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.example.rickandmorti.presentation.navigation.DetailCharacterComponent
import org.example.rickandmorti.presentation.screens.items.ItemEpisode
import org.example.rickandmorti.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    component: DetailCharacterComponent,
    modifier: Modifier = Modifier
) {
    val character by component.character.subscribeAsState()
    val episodes by component.episodes.subscribeAsState()
    val isEpisodeLoading by component.isEpisodeLoading.subscribeAsState()


    // 🟩 Отладка: логируем изменения состояния
    LaunchedEffect(episodes, isEpisodeLoading) {
        if (isEpisodeLoading) {
            Logger.log("DetailScreen: Loading episodes for ${character.name}...")
        } else {
            if (episodes.isEmpty()) {
                Logger.log("DetailScreen: No episodes found for ${character.name}")
            } else {
                Logger.log("DetailScreen: Loaded ${episodes.size} episodes for ${character.name}:")
                episodes.forEach { name ->
                    Logger.log("  • ${name.name}")
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .wrapContentWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = character.image,
                    contentDescription = "character image",
                    modifier = Modifier.padding(8.dp).clip(CircleShape).size(height = 120.dp, width = 120.dp),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Status: ",
                            fontWeight = FontWeight.Bold)
                        Text(character.status)
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    color = when(character.status) {
                                        "Alive" -> Color.Green
                                        "Dead" -> Color.Red
                                        else -> Color.Gray
                                    }),
                        )
                    }
                    Row {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Species: ",
                            fontWeight = FontWeight.Bold
                        )
                        Text(character.species)
                    }
                    Row {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Gender: ",
                            fontWeight = FontWeight.Bold
                        )
                        Text(character.gender)
                    }
                    Row {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Origin: ",
                            fontWeight = FontWeight.Bold
                        )
                        Text(character.origin.name, maxLines = 1)
                    }
                    Row {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Location: ",
                            fontWeight = FontWeight.Bold
                        )
                        Text(character.location.name, maxLines = 1)
                    }

                }
            }
        }
        // 🔹 Отображение эпизодов
        Text("Episodes:", style = MaterialTheme.typography.titleMedium)

        if (isEpisodeLoading) {
            Text("Loading episodes...")
        } else {
            if (episodes.isEmpty()) {
                Text("No episodes found.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(episodes) { episode ->
                        Column(modifier.padding(8.dp)) {
                            ItemEpisode(
                                episode = episode,
                                isFavorite = false,
                                onToggleFavorite = {},
                                onClick = { component.onEpisodeClicked(episode) }
                            )
                        }
                    }
                }
            }
        }
    }
}