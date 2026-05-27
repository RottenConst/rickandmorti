package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.example.rickandmorti.presentation.navigation.DetailEpisodeComponent
import org.example.rickandmorti.presentation.screens.items.ItemCharacter
import org.example.rickandmorti.util.Logger

@Composable
fun EpisodeDetailScreen(
    component: DetailEpisodeComponent,
    modifier: Modifier = Modifier
) {
    val episode by component.episode.subscribeAsState()
    val characters by component.characters.subscribeAsState()
    val isCharactersLoading by component.isCharactersLoading.subscribeAsState()
    val season = episode.episode.substring(1, 3).removePrefix("0").toIntOrNull() ?: 1
    val numberEpisode = episode.episode.substring(4).removePrefix("0").toIntOrNull() ?: 1

    LaunchedEffect(characters, isCharactersLoading) {
        if (isCharactersLoading) {
            Logger.log("DetailEpisode screen: Loading character for ${episode.name}")
        } else {
            if (characters.isEmpty()) {
                Logger.log("EpisodeDetail: No characters found for ${episode.name}")
            } else {
                Logger.log("EpisodeDetail: Loaded ${characters.size} characters for ${episode.name}:")
                characters.forEach { character ->
                    Logger.log(" - ${character.name}")
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
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = modifier.padding(16.dp)) {
                Text("Season №$season episode $numberEpisode", modifier = Modifier.padding(horizontal = 8.dp))
                Text("Name: ${episode.name}", modifier = Modifier.padding(horizontal = 8.dp))
                Text("Created ${episode.air_date}", modifier = Modifier.padding(horizontal = 8.dp))
                Button(onClick = {component.openEpisodeWatchPage()}, content = {Text("Open Watch Page")})
            }
        }

            Text("Characters in episode:", style = MaterialTheme.typography.titleMedium)

            if (isCharactersLoading) {
                Text("Loading character...")
            } else {
                if (characters.isEmpty()) {
                    Text("No characters found.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(characters) { character ->
                            ItemCharacter(
                                character = character,
                                isFavorite = false,
                                onToggleFavorite = {},
                                onClick = ({component.onCharacterClicked(character)})
                            )
                        }
                    }
                }
            }
        }
}