package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import compose.icons.tablericons.HeartBroken
import compose.icons.tablericons.Search
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.rickandmorti.presentation.navigation.CharacterListComponent
import org.example.rickandmorti.presentation.navigation.DefaultCharacterListComponent
import org.example.rickandmorti.presentation.screens.items.ItemCharacter
import org.example.rickandmorti.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    component: CharacterListComponent,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val debouncedQuery = remember { mutableStateOf("") }
    val state by component.characters.subscribeAsState()
    val isFavoritesOnly by component.isFavoritesOnly.subscribeAsState()
    val favorites by component.favorites.collectAsState(initial = emptySet())
    val listState = rememberLazyListState()
    var loadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        launch {
            delay(500)
            debouncedQuery.value = query
        }
    }

    LaunchedEffect(debouncedQuery.value, isFavoritesOnly) {
        component.loadNextPage(debouncedQuery.value.takeIf { it.isNotBlank() })
    }
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

    LaunchedEffect(state) {
        if (state.isNotEmpty()) {
            loadingMore = false
        }
    }


    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search...") },
                leadingIcon = {
                    Icon(
                        imageVector = TablerIcons.Search,
                        contentDescription = "Search",
                        tint = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
            )

            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .height(68.dp)
                    .padding(8.dp)
                    .background(
                        color = if (isFavoritesOnly) Color(0xFFFFEBEE) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clickable {
                        (component as? DefaultCharacterListComponent)?.toggleFavoritesOnly()
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavoritesOnly) TablerIcons.Heart else TablerIcons.HeartBroken,
                    contentDescription = if (isFavoritesOnly) "Show all" else "Show favorites only",
                    tint = if (isFavoritesOnly) Color.Red else LocalContentColor.current.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }


        LazyColumn(
            state = listState
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
    }

    // Сброс флага после подгрузки
    LaunchedEffect(state) {
        if (state.isNotEmpty()) {
            loadingMore = false
        }
    }
}