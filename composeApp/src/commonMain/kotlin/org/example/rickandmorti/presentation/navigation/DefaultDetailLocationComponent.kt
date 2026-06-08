package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.presentation.LocationViewModel
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultDetailLocationComponent(
    componentContext: ComponentContext,
    location: Location,
    private val characterClicked: (Character) -> Unit,
    private val onFinished: () -> Unit,
    private val favoritesStore: FavoritesStore
): DetailLocationComponent, ComponentContext by componentContext, KoinComponent {
    override val location: Value<Location> = MutableValue(location)


    private val _characters = MutableValue<List<Character>>(emptyList())
    override val characters: Value<List<Character>> = _characters

    private val _favorites = MutableValue<Set<Int>>(emptySet())
    override val favorites: Value<Set<Int>> = _favorites

    private val _isCharactersLoading = MutableValue(true)
    override val isCharactersLoading: Value<Boolean> = _isCharactersLoading

    private val scope = CoroutineScope(SupervisorJob())

    private val viewModel: LocationViewModel = get<LocationViewModel> {
        parametersOf()
    }

    init {
        Logger.log("DefaultDetailLocationComponent location.residents ${location.residents}")
        viewModel.loadResidents(location.residents)

        viewModel.stateCharacter
            .onEach { stateCharacter ->
                when(stateCharacter) {
                    is UiStateCharacter.Loading -> {
                        _isCharactersLoading.value = true
                    }
                    is UiStateCharacter.Success -> {
                        Logger.log("loaded residents ${stateCharacter.characters.size}")
                        _characters.value = stateCharacter.characters
                        _isCharactersLoading.value = false
                    }
                    is UiStateCharacter.Error -> {
                        _isCharactersLoading.value = false
                    }
                }
            }.launchIn(scope)

        favoritesStore.favoritesLocationsFlow
            .onEach { set ->
                _favorites.value = set
            }.launchIn(scope)

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override fun toggleFavorite(location: Location) {
        if (favoritesStore.isFavoriteLocation(location.id)) {
            favoritesStore.removeFavoriteLocation(location.id)
        } else {
            favoritesStore.addFavoriteLocation(location.id)
        }
    }

    override fun onCharacterClicked(character: Character) = characterClicked(character)

    override fun onBackPressed() = onFinished()
}