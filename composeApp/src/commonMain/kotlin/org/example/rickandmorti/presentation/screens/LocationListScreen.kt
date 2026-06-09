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
import org.example.rickandmorti.presentation.navigation.DefaultLocationListComponent
import org.example.rickandmorti.presentation.navigation.LocationListComponent
import org.example.rickandmorti.presentation.screens.items.ItemLocation
import org.example.rickandmorti.util.Logger
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LocationListScreen(
    component: LocationListComponent,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val debouncedQuery = remember { mutableStateOf("") }

    val locations by component.location.subscribeAsState()
    val isFavoritesOnly by component.isFavoritesOnly.subscribeAsState()
    val favorites by component.favorites.collectAsState(emptySet())

    val listState = rememberLazyListState()
    var loadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        launch {
            delay(500.milliseconds)
            debouncedQuery.value = query
        }
    }

    LaunchedEffect(debouncedQuery.value, isFavoritesOnly) {
        component.loadSearchLocation(debouncedQuery.value.takeIf { it.isNotBlank() })
    }
    val hasMorePages by component.hasMorePages.subscribeAsState()
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
                    Logger.log("Location List Screen Load More")
                    loadingMore = true
                    component.loadNextPage(debouncedQuery.value.takeIf { it.isNotBlank() })
                }
            }
    }

    LaunchedEffect(locations) {
        if (locations.isNotEmpty()) {
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
                colors = TextFieldDefaults.colors(
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
                        color = if (isFavoritesOnly) Color(0xFFFFEBEE) else Color(0xFFE4DEE7),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clickable {
                        (component as? DefaultLocationListComponent)?.toggleFavoriteOnly()
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
            if (locations.isEmpty() && !loadingMore) {
                item {
                    Text(
                        text = "No locations loaded",
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                            .padding(16.dp),
                        color = Color.Red
                    )
                }
            }

            items(locations) { location ->
                ItemLocation(
                    location = location,
                    isFavorite = favorites.contains(location.id),
                    onToggleFavorite =  { component.toggleFavorite(location)},
                    onClick = {
                        component.onLocationClick(location)
                    }
                )
            }


            if (loadingMore && hasMorePages) {
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
}