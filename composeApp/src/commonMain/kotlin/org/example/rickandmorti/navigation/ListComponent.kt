package org.example.rickandmorti.navigation

import com.arkivanov.decompose.ComponentContext
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.example.rickandmorti.FavoritesStore
import kotlin.collections.emptySet

class DefaultListComponent(
    componentContext: ComponentContext,
    private val characterClicked: (Character) -> Unit,
    override val isFavoritesOnly: Boolean = false,
    private val favoritesStore: FavoritesStore
): ListComponent, ComponentContext by componentContext, KoinComponent {

    private val _allCharacters = MutableValue<List<Character>>(emptyList())
    private val _filteredCharacters = MutableValue<List<Character>>(emptyList())

    override val model: Value<List<Character>> = _filteredCharacters

    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesFlow
    private var currentFavorites: Set<Int> = emptySet()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null

    private val viewModel: GreetingViewModel = get<GreetingViewModel> {
        parametersOf("Hello! KMP")
    }

    init {
        Logger.log("ListComponent: init started")

        // Слушаем изменения персонажей
        viewModel.characters
            .onEach { characters ->
                Logger.log("Received ${characters.size} characters from Flow")
                _allCharacters.update { characters }
            }
            .launchIn(scope)

        // Слушаем изменения избранного
        favoritesStore.favoritesFlow
            .distinctUntilChanged()
            .onEach { favorites ->
                currentFavorites = favorites
                updateFiltered()
            }
            .launchIn(scope)

        // Обновляем фильтрацию при изменении всех персонажей
        _allCharacters.subscribe { updateFiltered() }

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
                        _allCharacters.update { characters }
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

    private fun updateFiltered() {
        val all = _allCharacters.value
        val filtered = if (!isFavoritesOnly) {
            all
        } else {
            all.filter { currentFavorites.contains(it.id) }
        }
        _filteredCharacters.value = filtered
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