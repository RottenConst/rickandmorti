package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.collections.emptySet

class DefaultDetailCharacterComponent(
    componentContext: ComponentContext,
    character: Character,
    private val episodeClicked: (Episode) -> Unit,
    private val onLocationToScreenClicked: (Location) -> Unit,
    private val onFinished: () -> Unit,
    private val favoritesStore: FavoritesStore
): DetailCharacterComponent, ComponentContext by componentContext, KoinComponent {

    override val character: Value<Character> = MutableValue(character)

    private val _episodes = MutableValue<List<Episode>>(emptyList())
    override val episodes: Value<List<Episode>> = _episodes

    private val _isEpisodeLoading = MutableValue(true)
    override val isEpisodeLoading: Value<Boolean> = _isEpisodeLoading
    private val _favorites = MutableValue<Set<Int>>(emptySet())
    override val favorites: Value<Set<Int>> = _favorites

    private val scope = CoroutineScope(SupervisorJob())
    private val clickScope = CoroutineScope(SupervisorJob() + Main)

    private val viewModel: CharacterViewModel = get<CharacterViewModel> {
        parametersOf()
    }

    init {
        Logger.log("🔍 DefaultDetailComponent: character.episodes = ${character.episode}")
        viewModel.loadEpisodes(character.episode)

        viewModel.stateEpisode
            .onEach { state ->
                when (state) {
                    is UiStateEpisode.Loading -> {
                        _isEpisodeLoading.value = true
                    }
                    is UiStateEpisode.Success -> {
                        Logger.log("loaded episod ${state.episodes.size}")
                        _episodes.value = state.episodes
                        _isEpisodeLoading.value = false
                    }
                    is UiStateEpisode.Error -> {
                        _isEpisodeLoading.value = false
                    }
                }
            }
            .launchIn(scope)

        favoritesStore.favoritesCharactersFlow
            .onEach { set ->
                _favorites.value = set
            }
            .launchIn(scope)

        lifecycle.doOnDestroy {
            scope.cancel()
            clickScope.cancel()
        }
    }

    override fun onLocationClicked(locationUrl: String) {
        clickScope.launch {
            viewModel.getLocation(locationUrl, false)
            viewModel.location
                .collect { location ->
                    if (location != null) {
                        onLocationToScreenClicked(location)
                    } else {
                        Logger.log("location not fount")
                    }
                }
        }
    }

    override fun onEpisodeClicked(episode: Episode) = episodeClicked(episode)

    override fun toggleFavorite(character: Character) {
        if (favoritesStore.isFavoriteCharacter(character.id)) {
            favoritesStore.removeFavoriteCharacter(character.id)
        } else {
            favoritesStore.addFavoriteCharacter(character.id)
        }
    }

    override fun onBackPressed() = onFinished()
}