package org.example.rickandmorti.presentation.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compose.icons.TablerIcons
import compose.icons.tablericons.Search
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.example.rickandmorti.presentation.navigation.CharacterListComponent
import org.example.rickandmorti.presentation.screens.items.ItemCharacter
import org.example.rickandmorti.presentation.screens.items.PortalProgress
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
                    HeroHeader()
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    SearchPortalBar(
                        query = query,
                        onQueryChange = { query = it}
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
                            PortalProgress()
                        }
                    }
                }
            }
        }
    }

@Composable
fun HeroHeader(
    modifier: Modifier = Modifier,
    characterCount: Int = 826
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF10151B))
    ) {

        AnimatedSpaceBackground()

        PortalDecoration(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
        )

        HeroGradient()

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {

            Text(
                text = "Rick & Morty",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Explore Infinite Universes",
                color = Color(0xFFA8B2BD)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "$characterCount Characters",
                color = Color(0xFF76FF03),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AnimatedSpaceBackground() {

    val infiniteTransition =
        rememberInfiniteTransition()

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 250f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = 30000,
                easing = LinearEasing
            )
        )
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        repeat(120) { i ->

            val x =
                ((i * 97f + offset) % size.width)

            val y =
                ((i * 53f) % size.height)

            drawCircle(
                color = Color.White.copy(
                    alpha = .4f
                ),
                radius = 1.5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun PortalDecoration(
    modifier: Modifier = Modifier
) {

    val infiniteTransition =
        rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(
            tween(
                12000,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier
            .size(180.dp)
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {

        PortalProgress(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HeroGradient() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xCC10151B),
                        Color(0x9910151B),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun SearchPortalBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = TablerIcons.Search,
                null
            )
        },
        placeholder = {
            Text(text = "Search dimension...", color = Color.White)
        },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF182028),
            unfocusedContainerColor = Color(0xFF182028)
        )
    )
}

@Composable
fun adaptiveGridCells(): GridCells {

    val width = LocalWindowInfo.current.containerSize.width.dp

    return when {

        width < 600.dp ->
            GridCells.Fixed(2)

        width < 900.dp ->
            GridCells.Adaptive(220.dp)

        else ->
            GridCells.Adaptive(280.dp)
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