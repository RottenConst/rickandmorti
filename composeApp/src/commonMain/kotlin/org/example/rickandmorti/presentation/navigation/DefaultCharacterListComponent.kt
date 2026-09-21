package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.util.Logger
import kotlin.collections.emptySet

class DefaultCharacterListComponent(
    componentContext: ComponentContext,
    private val characterClicked: (Character) -> Unit,
    private val favoritesStore: FavoritesStore,
    private val viewModel: CharacterViewModel
): CharacterListComponent, ComponentContext by componentContext{

    private val componentScope = CoroutineScope(SupervisorJob())

    private val _allCharacters = MutableValue<List<Character>>(emptyList())
    private val _filteredCharacters = MutableValue<List<Character>>(emptyList())

    private val _isFavoritesOnly = MutableValue(false)
    override val isFavoritesOnly: Value<Boolean> = _isFavoritesOnly

    private val _hasMorePages = MutableValue(true)
    override val hasMorePages: Value<Boolean> = _hasMorePages

    override val characters: Value<List<Character>> = _filteredCharacters

    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesCharactersFlow
    private var currentFavorites: Set<Int> = emptySet()

    init {
        Logger.log("ListComponent: init started")

        // Слушаем изменения персонажей
        setupCharacterFlow()
        setupFavoritesFlow()

        // Обновляем фильтрацию при изменении всех персонажей
        _allCharacters.subscribe { updateFiltered() }

        componentContext.lifecycle.doOnCreate {
            Logger.log("ListComponent: onCreate")
            viewModel.loadedAllCharacters()
        }
        componentContext.lifecycle.doOnDestroy {
            Logger.log("ListComponent: onDestroy")
            componentScope.cancel()
        }
    }

    private fun setupCharacterFlow() {
        componentScope.launch {
            viewModel.stateCharacter.collect { state ->
                when(state) {
                    is UiStateCharacter.Success -> {
                        Logger.log("Received ${state.characters.size} characters from Flow")
                        _allCharacters.update { state.characters }
                        _hasMorePages.value = state.hasMorePages
                    }
                    is UiStateCharacter.Loading -> _hasMorePages.value = true
                    is UiStateCharacter.Error -> _hasMorePages.value = false
                }
            }
        }
    }

    private fun setupFavoritesFlow() {
        componentScope.launch {
            favoritesStore.favoritesCharactersFlow
                .distinctUntilChanged { old, new -> old == new }
                .collect { favorites ->
                    currentFavorites = favorites
                    updateFiltered()
                }
        }
    }

    private fun updateFiltered() {
        val all = _allCharacters.value
        val filtered = if (!isFavoritesOnly.value) {
            all
        } else {
            all.filter { currentFavorites.contains(it.id) }
        }

        if (filtered !== _filteredCharacters.value) {
            _filteredCharacters.value = filtered
        }
    }

    override fun onCharacterClicked(character: Character) = characterClicked(character)

    // Метод для подгрузки следующей страницы
    override fun loadNextPage() {
        viewModel.loadedAllCharacters()
    }

    override fun toggleFavorite(character: Character) {
        if (favoritesStore.isFavoriteCharacter(character.id)) {
            favoritesStore.removeFavoriteCharacter(character.id)
        } else {
            favoritesStore.addFavoriteCharacter(character.id)
        }
    }

    override fun toggleFavoritesOnly() {
        _isFavoritesOnly.update { !it }
        if (isFavoritesOnly.value) {
            loadMissingFavorites()
        } else {
            updateFiltered()
        }
    }

    private fun loadMissingFavorites() {
        val loadedIds = _allCharacters.value.map { it.id }.toSet()
        val missingIds = currentFavorites - loadedIds
        
        if (missingIds.isNotEmpty()) {
            Logger.log("Missing ${missingIds.size} favorites, loading...")
            viewModel.loadCharactersByIds(missingIds.toList())
        }
        
        updateFiltered()
    }

    override fun loadSearch(name: String?) {
        componentScope.launch {
            viewModel.refreshWithSearch(name)
        }
    }

    override fun loadNextPage(name: String?) {
        viewModel.loadNextPage(name)
    }

}