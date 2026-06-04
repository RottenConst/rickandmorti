package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.example.rickandmorti.presentation.navigation.DetailLocationComponent
import org.example.rickandmorti.presentation.screens.items.ItemCharacter
import org.example.rickandmorti.util.Logger

@Composable
fun LocationDetailScreen(
    component: DetailLocationComponent,
    modifier: Modifier = Modifier
) {
    val location by component.location.subscribeAsState()
    val residents by component.characters.subscribeAsState()
    val isCharactersLoading by component.isCharactersLoading.subscribeAsState()

    LaunchedEffect(residents, isCharactersLoading) {
        if (isCharactersLoading) {
            Logger.log("Detail Location Screen: Loading resident for ${location.name}")
        } else {
            if (residents.isEmpty()) {
                Logger.log("Location detail screen : No Residence for location ${location.name}")
            } else {
                Logger.log("Location details loaded residents ${residents.size} for ${location.name}")
                residents.forEach { character ->
                    Logger.log("- ${character.name}")
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
            Column(
                modifier = modifier.padding(16.dp)
            ) {
                Text("Name: ${location.name}", modifier = Modifier.padding(8.dp))
                Text("Dimension: ${location.dimension}", modifier = Modifier.padding(8.dp))
                Text("Type: ${location.type}", modifier = Modifier.padding(8.dp))
            }
        }

        Text("Residents of this place", style=MaterialTheme.typography.titleMedium)

        if (isCharactersLoading) {
            Text("Loading residents...")
        } else {
            if (residents.isEmpty()) {
                Text("No residets found")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(residents) { character ->
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