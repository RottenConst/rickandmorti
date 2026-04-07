package org.example.rickandmorti.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import org.example.rickandmorti.navigation.DetailComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    component: DetailComponent,
) {
    val state by component.model.subscribeAsState()

    Scaffold(
        modifier = Modifier.padding(8.dp),
        topBar = {
            TopAppBar(
                title = {Text("Character Details")},
                navigationIcon = {
                    IconButton(onClick = component::onBackPressed) {
                        Icon(imageVector = TablerIcons.ArrowLeft, contentDescription = "back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            AsyncImage(
                model = state.image,
                contentDescription = "character image",
                modifier = Modifier
                    .padding(top = 32.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )

            Text("Name: ${state.name}", style = MaterialTheme.typography.headlineMedium)
            Text("Status: ${state.status}")
            Text("Species: ${state.species}")
            Text("Gender: ${state.gender}")
            Text("Origin: ${state.origin.name}")
            Text("Location: ${state.location.name}")
        }

    }
}