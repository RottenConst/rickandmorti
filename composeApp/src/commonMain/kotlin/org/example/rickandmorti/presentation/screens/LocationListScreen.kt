package org.example.rickandmorti.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import compose.icons.tablericons.Search
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.rickandmorti.presentation.navigation.LocationListComponent
import org.example.rickandmorti.presentation.screens.items.ItemLocation
import org.example.rickandmorti.util.Logger

@Composable
fun LocationListScreen(
    component: LocationListComponent,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val debouncedQuery = remember { mutableStateOf("") }

    val state by component.location.subscribeAsState()

    val listState = rememberLazyListState()
    var loadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        launch {
            delay(500)
            debouncedQuery.value = query
        }
    }

    LaunchedEffect(debouncedQuery.value) {
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
        }

        LazyColumn(
            state = listState
        ) {
            if (state.isEmpty() && !loadingMore) {
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

            items(state) {location ->
                ItemLocation(
                    location = location,
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

    LaunchedEffect(state) {
        if (state.isNotEmpty()) {
            loadingMore = false
        }
    }
}