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
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.openInBrowser
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultDetailEpisodeComponent(
    componentContext: ComponentContext,
    episode: Episode,
    private val characterClicked: (Character) -> Unit,
    private val onFinished: () -> Unit
): DetailEpisodeComponent, ComponentContext by componentContext, KoinComponent {

    override val episode: Value<Episode> = MutableValue(episode)

    private val _characters = MutableValue<List<Character>>(emptyList())
    override val characters: Value<List<Character>> = _characters

    private val _isCharactersLoaded = MutableValue(true)
    override val isCharactersLoading: Value<Boolean> = _isCharactersLoaded

    private val scope = CoroutineScope(SupervisorJob())
    private val viewModel: CharacterViewModel = get<CharacterViewModel> {
        parametersOf()
    }

    init {
        Logger.log("DefaultDetailEpisodeComponent episode.character = ${episode.characters}")
        viewModel.loadedCharacter(episode.characters)

        viewModel.stateCharacter
            .onEach { state ->
                when(state) {
                    is CharacterViewModel.UiStateCharacter.Loading -> {
                        _isCharactersLoaded.value = true
                    }
                    is CharacterViewModel.UiStateCharacter.Success -> {
                        Logger.log("loaded character ${state.characters.size}")
                        _characters.value = state.characters
                        _isCharactersLoaded.value = false
                    }
                    is CharacterViewModel.UiStateCharacter.Error -> {
                        _isCharactersLoaded.value = false
                    }
                }
            }
            .launchIn(scope)

        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override fun onCharacterClicked(character: Character) = characterClicked(character)

    override fun openEpisodeWatchPage() {
        val episodeCode = episode.value.episode // или используем episode.value.episode ("S04E19")
        val season = episodeCode.substring(1, 3).toIntOrNull() ?: return
        val episodeNumber = episodeCode.substring(4).toIntOrNull() ?: return
        val watchUrl = "https://rick-i-morty.com/episodes/${season}sez-${episodeNumber}seriya/"

        openInBrowser(watchUrl)
    }

    override fun onBackPressed() = onFinished()
}