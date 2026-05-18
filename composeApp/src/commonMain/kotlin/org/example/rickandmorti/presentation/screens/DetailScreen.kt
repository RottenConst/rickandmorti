package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.example.rickandmorti.presentation.navigation.DetailComponent
import org.example.rickandmorti.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    component: DetailComponent,
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

        AsyncImage(
            model = character.image,
            contentDescription = "character image",
            modifier = Modifier.padding(8.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = modifier.padding(16.dp)) {
                Text("Status: ${character.status}", modifier = Modifier.padding(horizontal = 8.dp))
                Text("Species: ${character.species}", modifier = Modifier.padding(horizontal = 8.dp))
                Text("Gender: ${character.gender}", modifier = Modifier.padding(horizontal = 8.dp))
                Text("Origin: ${character.origin.name}", modifier = Modifier.padding(horizontal = 8.dp))
                Text("Location: ${character.location.name}", modifier = Modifier.padding(horizontal = 8.dp))
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(episodes) { episode ->

                            Column(modifier.padding(8.dp)) {
                                Text("• ${episode.name}")
                            }
                        }
                    }
                }
            }
        }

    }
}