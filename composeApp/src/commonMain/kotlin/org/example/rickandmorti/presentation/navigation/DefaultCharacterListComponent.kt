package org.example.rickandmorti.presentation.navigation

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
import org.example.rickandmorti.domain.model.Character
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.util.Logger
import kotlin.collections.emptySet
import kotlin.time.Duration.Companion.milliseconds

class DefaultCharacterListComponent(
    componentContext: ComponentContext,
    private val characterClicked: (Character) -> Unit,
    private val favoritesStore: FavoritesStore
): CharacterListComponent, ComponentContext by componentContext, KoinComponent {

    private val _allCharacters = MutableValue<List<Character>>(emptyList())
    private val _filteredCharacters = MutableValue<List<Character>>(emptyList())

    private val _isFavoritesOnly = MutableValue(false)
    override val isFavoritesOnly: Value<Boolean> = _isFavoritesOnly
    private val _hasMorePages = MutableValue(true)
    override val hasMorePages: Value<Boolean> = _hasMorePages

    override val characters: Value<List<Character>> = _filteredCharacters

    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesCharactersFlow
    private var currentFavorites: Set<Int> = emptySet()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null

    private val viewModel: CharacterViewModel = get<CharacterViewModel> {
        parametersOf()
    }

    init {
        Logger.log("ListComponent: init started")

        // Слушаем изменения персонажей
        viewModel.stateCharacter
            .onEach { state ->
                when (state) {
                    is UiStateCharacter.Success -> {
                        Logger.log("Received ${state.characters.size} characters from Flow")
                        _allCharacters.update { state.characters }
                    }
                    is UiStateCharacter.Loading -> {
                        _hasMorePages.value = true
                    }
                    is UiStateCharacter.Error -> {
                        _hasMorePages.value = false
                    }
                }

            }
            .launchIn(scope)

        // Слушаем изменения избранного
        favoritesStore.favoritesCharactersFlow
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
                    viewModel.loadedAllCharacters()
                }
            },
            onDestroy = {
                Logger.log("ListComponent: onDestroy")
                scope.cancel()
            }
        )
    }

    private fun updateFiltered() {
        val all = _allCharacters.value
        val filtered = if (!isFavoritesOnly.value) {
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
            delay(100.milliseconds)
            viewModel.loadedAllCharacters()
        }
    }

    override fun toggleFavorite(character: Character) {
        if (favoritesStore.isFavoriteCharacter(character.id)) {
            favoritesStore.removeFavoriteCharacter(character.id)
        } else {
            favoritesStore.addFavoriteCharacter(character.id)
        }
    }

    fun toggleFavoritesOnly() {
        _isFavoritesOnly.update { !it }
        updateFiltered()
    }

    override fun loadSearch(name: String?) {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(300.milliseconds)
            viewModel.refreshWithSearch(name)
        }
    }

    override fun loadNextPage(name: String?) {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(300.milliseconds)
            viewModel.loadNextPage(name)
        }
    }

}