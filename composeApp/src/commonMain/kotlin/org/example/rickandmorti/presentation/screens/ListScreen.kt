package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import org.example.rickandmorti.presentation.navigation.ListComponent
import org.example.rickandmorti.presentation.screens.items.ItemCharacter
import org.example.rickandmorti.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    component: ListComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    val favorites by component.favorites.collectAsState(initial = emptySet())
    val listState = rememberLazyListState()

    var loadingMore by remember { mutableStateOf(false) }
    val title = if (component.isFavoritesOnly) "Favorites" else "Characters"

    // Создаем derived state для отслеживания условия прокрутки
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            !loadingMore && totalItems > 0 && lastVisibleItemIndex >= totalItems - 5
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
                    component.loadNextPage()
                }
            }
    }


        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
        ) {
            // Сообщение, если список пуст
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

            // Элементы списка
            items(state) { character ->
                ItemCharacter(
                    character = character,
                    isFavorite = favorites.contains(character.id),
                    onToggleFavorite = { component.toggleFavorite(it) },
                    onClick = { component.onCharacterClicked(character) }
                )
            }

            // Индикатор загрузки
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

    // Сброс флага после подгрузки
    LaunchedEffect(state) {
        if (state.isNotEmpty()) {
            loadingMore = false
        }
    }
}