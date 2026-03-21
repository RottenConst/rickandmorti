package org.example.rickandmorti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import okio.FileSystem
import org.example.rickandmorti.data.Character
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

@Composable
@Preview
fun App() {
    MaterialTheme {

        val koin = getKoin()

        val viewModel: GreetingViewModel = remember {
            koin.get<GreetingViewModel> {
                parametersOf("Welcome! kmp")
            }
        }

        var greetings by remember { mutableStateOf(viewModel.greetings.value) }
        val characters by viewModel.characters.collectAsState()

        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .crossfade(true)
                .logger(DebugLogger())
                .build()
        }

        LaunchedEffect(Unit) {
            viewModel.greetings.collect { newGreetings ->
                greetings = newGreetings
            }
        }

        LaunchedEffect(Unit) {
            viewModel.loadCharacters()
        }

        Column(
            modifier = Modifier
                .padding(all = 10.dp)
                .safeContentPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            greetings.forEach { greeting ->
                Text(greeting)
                HorizontalDivider()
            }

            Button(onClick = {
                viewModel.addGreeting("New greeting at ")
            }) {
                Text("add greeting")
            }

            Spacer(Modifier.size(16.dp))

            if (characters.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(characters) { character ->
                        CharacterItem(character = character)

                    }
                }
            }
        }
    }
}


@Composable
private fun CharacterItem(character: Character) {

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        AsyncImage(
            model = character.image,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("${character.name} (${character.status}) - ${character.species}")
    }
    HorizontalDivider()
}

fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context).crossfade(true).logger(DebugLogger()).build()

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(1024L * 1024 * 1024) // 512MB
        .build()
}