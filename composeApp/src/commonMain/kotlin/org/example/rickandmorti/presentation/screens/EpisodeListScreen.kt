package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import org.example.rickandmorti.presentation.navigation.EpisodeListComponent
import org.example.rickandmorti.presentation.screens.items.ItemEpisode
import org.example.rickandmorti.util.Logger

@Composable
fun EpisodeListScreen(
    component: EpisodeListComponent,
    modifier: Modifier = Modifier
) {
    val state by component.episodes.subscribeAsState()
    val listState = rememberLazyListState()

    var loadingMore by remember { mutableStateOf(false) }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibilityItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            !loadingMore && totalItems > 0 && lastVisibilityItemIndex >= totalItems - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { loadMore ->
                if (loadMore) {
                    Logger.log("Episode list: Scrolled near end, loading more...")
                    loadingMore = true
                    component.loadNextPage()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        if (state.isEmpty() && !loadingMore) {
            item {
                Text(
                    text = "No episodes loaded.",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                        .padding(16.dp),
                    color = Color.Red
                )
            }
        }

        items(state) { episode ->
            ItemEpisode(
                episode = episode,
                onClick = { component.onEpisodeClicked(episode)}
            )
        }

        if (loadingMore) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .wrapContentSize()
                )
            }
        }
    }

    LaunchedEffect(state) {
        if (state.isNotEmpty()) {
            loadingMore = false
        }
    }
}