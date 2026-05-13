package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Heart
import org.example.rickandmorti.presentation.navigation.DetailComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    component: DetailComponent,
    modifier: Modifier = Modifier
) {
    val character by component.model.subscribeAsState()

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
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )

        Text("Name: ${character.name}", style = MaterialTheme.typography.headlineMedium)
        Text("Status: ${character.status}")
        Text("Species: ${character.species}")
        Text("Gender: ${character.gender}")
        Text("Origin: ${character.origin.name}")
        Text("Location: ${character.location.name}")

    }
}