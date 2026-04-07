package org.example.rickandmorti.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.rickandmorti.GreetingViewModel
import org.example.rickandmorti.Logger
import org.example.rickandmorti.data.Character
import org.example.rickandmorti.screens.items.ItemCharacter
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.example.rickandmorti.FavoritesStore

class DefaultListComponent(
    componentContext: ComponentContext,
    private val characterClicked: (Character) -> Unit,
    private val favoritesStore: FavoritesStore
): ListComponent, ComponentContext by componentContext, KoinComponent {

    private val _model = MutableValue<List<Character>>(emptyList())
    override val model: Value<List<Character>> = _model
    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesFlow

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null

    private val viewModel: GreetingViewModel = get<GreetingViewModel> {
        parametersOf("Hello! KMP")
    }

    init {
        Logger.log("ListComponent: init started")
        lifecycle.subscribe(
            onCreate = {
                Logger.log("ListComponent: onCreate")
                scope.launch {
                    viewModel.loadCharacters()
                }
                // Подписка на поток данных
                scope.launch {
                    viewModel.characters.collect { characters ->
                        Logger.log("Received ${characters.size} characters from Flow")
                        _model.update { characters }
                    }
                }
            },
            onDestroy = {
                Logger.log("ListComponent: onDestroy")
                viewModel.onDestroy()
                scope.cancel()
            }
        )
    }

    override fun onCharacterClicked(character: Character) = characterClicked(character)

    // Метод для подгрузки следующей страницы
    override fun loadNextPage() {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            // Небольшая задержка, чтобы избежать множественных вызовов
            delay(100)
            viewModel.loadNextPage()
        }
    }

    override fun toggleFavorite(character: Character) {
        if (favoritesStore.isFavorite(character.id)) {
            favoritesStore.removeFavorite(character.id)
        } else {
            favoritesStore.addFavorite(character.id)
        }
    }
}