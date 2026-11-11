package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.example.rickandmorti.presentation.navigation.CharacterListComponent
import org.example.rickandmorti.presentation.screens.items.CharacterFilter
import org.example.rickandmorti.presentation.screens.items.CharacterFilters
import org.example.rickandmorti.presentation.screens.items.HeroHeaderCollapse
import org.example.rickandmorti.presentation.screens.items.ItemCharacter
import org.example.rickandmorti.presentation.screens.items.PortalIndicator
import org.example.rickandmorti.presentation.screens.items.SearchPortalBar
import org.example.rickandmorti.util.Logger
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CharacterListScreen(
    component: CharacterListComponent
) {
    var query by remember { mutableStateOf("") }
    val debouncedQuery = rememberDebouncedString(value = query)

    val state by component.characters.subscribeAsState()
    val isFavoritesOnly by component.isFavoritesOnly.subscribeAsState()
    val favorites by component.favorites.collectAsState(initial = emptySet())
    val hasMorePages by component.hasMorePages.subscribeAsState()

    val listState = rememberLazyGridState()
    var loadingMore by remember { mutableStateOf(false) }

    var selectedFilter by remember {
        mutableStateOf(
            CharacterFilter.ALL
        )
    }

    LaunchedEffect(debouncedQuery.value, isFavoritesOnly) {
        component.loadSearch(debouncedQuery.value.takeIf { it.isNotBlank() })
    }
    // Создаем derived state для отслеживания условия прокрутки
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            !loadingMore && hasMorePages && totalItems > 0 && lastVisibleItemIndex >= totalItems - 5
        }
    }

    // Отслеживаем изменение условия
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { loadMore ->
                if (loadMore) {
                    Logger.log("ListContent: Scrolled near end, loading more...")
                    loadingMore = true
                    component.loadNextPage(debouncedQuery.value.takeIf { it.isNotBlank() })
                }
            }
    }

    LaunchedEffect(state) {
        loadingMore = false
    }

        BoxWithConstraints {
            val minSize = when {
                maxWidth < 600.dp -> 170.dp
                maxWidth < 840.dp -> 220.dp
                else -> 280.dp
            }

            LazyVerticalGrid(
                state = listState,
                columns = GridCells.Adaptive(minSize = minSize),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HeroHeaderCollapse(
                        gridState = listState,
                        state.size
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    SearchPortalBar(
                        query = query,
                        onQueryChange = { query = it}
                    )
                }

                item(span = { GridItemSpan(maxLineSpan)}) {
                    CharacterFilters(
                        selected = selectedFilter,
                        onSelected = { selectedFilter = it }
                    )
                }

                if (state.isEmpty() && !loadingMore) {
                    item {
                        Text(
                            text = "No characters loaded.",
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize()
                                .padding(16.dp),
                            color = Color.Red
                        )
                    }
                }


                items(state, key = { it.id }) { character ->
                    ItemCharacter(
                        character = character,
                        isFavorite = favorites.contains(character.id),
                        onToggleFavorite = { component.toggleFavorite(it) },
                        onClick = { component.onCharacterClicked(character) }
                    )
                }

                if (loadingMore && hasMorePages) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PortalIndicator()
                        }
                    }
                }
            }
        }
}


@Composable
private fun rememberDebouncedString(
    value: String,
): MutableState<String> {
    val debouncedValue = remember { mutableStateOf(value) }
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(value) {
        job?.cancel()
        delay(500.milliseconds)
        debouncedValue.value = value
    }

    return debouncedValue
}