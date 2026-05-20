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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultEpisodeListComponent(
    componentContext: ComponentContext,
    private val episodeClicked: (Episode) -> Unit,
): EpisodeListComponent, ComponentContext by componentContext, KoinComponent {

    private val _allEpisodes = MutableValue<List<Episode>>(emptyList())
    override val episodes: Value<List<Episode>> = _allEpisodes
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null
    private val viewModel: CharacterViewModel = get<CharacterViewModel> {
        parametersOf()
    }

    init {
        Logger.log("List Episode: init started")

        viewModel.stateEpisode
            .onEach { state ->
                when(state) {
                    is CharacterViewModel.UiStateEpisode.Success -> {
                        Logger.log("Received ${state.episodes.size} episodes from Flow")
                        _allEpisodes.update { state.episodes }
                    }
                    is CharacterViewModel.UiStateEpisode.Loading -> {}
                    is CharacterViewModel.UiStateEpisode.Error -> {}
                }
            }.launchIn(scope)

        lifecycle.subscribe(
            onCreate = {
                Logger.log("Episode list Component: onCreate")
                scope.launch {
                    viewModel.loadedAllCharacters()
                }
            },
            onDestroy = {
                Logger.log("Episode list Component: onDestroy")
                scope.cancel()
            }
        )
    }

    override fun onEpisodeClicked(episode: Episode) = episodeClicked(episode)

    override fun loadNextPage() {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(1000)
            viewModel.loadAllEpisodes()
        }
    }
}