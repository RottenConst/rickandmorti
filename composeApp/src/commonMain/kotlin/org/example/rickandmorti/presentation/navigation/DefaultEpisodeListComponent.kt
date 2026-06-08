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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.presentation.EpisodesViewModel
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

class DefaultEpisodeListComponent(
    componentContext: ComponentContext,
    private val episodeClicked: (Episode) -> Unit,
    private val favoritesStore: FavoritesStore
): EpisodeListComponent, ComponentContext by componentContext, KoinComponent {

    private val _allEpisodes = MutableValue<List<Episode>>(emptyList())
    private val _filteredEpisodes = MutableValue<List<Episode>>(emptyList())

    private val _isFavoritesOnly = MutableValue(false)
    override val isFavoritesOnly: Value<Boolean> = _isFavoritesOnly

    private val _hasMorePages = MutableValue(true)
    override val hasMorePages: Value<Boolean> = _hasMorePages

    override val episodes: Value<List<Episode>> = _filteredEpisodes
    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesEpisodesFlow
    private var currentFavorites: Set<Int> = emptySet()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null
    private val viewModel: EpisodesViewModel = get<EpisodesViewModel> {
        parametersOf()
    }

    init {
        Logger.log("List Episode: init started")

        // Слушаем изменения эпизодов
        viewModel.stateEpisode
            .onEach { state ->
                when (state) {
                    is UiStateEpisode.Success -> {
                        Logger.log("Received ${state.episodes.size} episodes from Flow")
                        _allEpisodes.update { state.episodes }
                    }
                    is UiStateEpisode.Loading -> {
                        _hasMorePages.value = true
                    }
                    is UiStateEpisode.Error -> {
                        _hasMorePages.value = false
                    }
                }
            }
            .launchIn(scope)

        // Слушаем изменения избранного
        favoritesStore.favoritesEpisodesFlow
            .distinctUntilChanged()
            .onEach { favorites ->
                currentFavorites = favorites
                updateFiltered()
            }
            .launchIn(scope)

        // Обновляем фильтрованный список при изменении всех эпизодов
        _allEpisodes.subscribe { updateFiltered() }

        lifecycle.subscribe(
            onCreate = {
                Logger.log("Episode list Component: onCreate")
                // loadAllEpisodes уже вызван в init, обновляем hasMorePages
                _hasMorePages.value = true
            },
            onDestroy = {
                Logger.log("Episode list Component: onDestroy")
                scope.cancel()
            }
        )
    }

    private fun updateFiltered() {
        val all = _allEpisodes.value
        val isFavoritesOnly = _isFavoritesOnly.value
        val filtered = if (!isFavoritesOnly) {
            all
        } else {
            all.filter { currentFavorites.contains(it.id) }
        }
        _filteredEpisodes.value = filtered
    }

    override fun onEpisodeClicked(episode: Episode) = episodeClicked(episode)

    override fun loadNextPage() {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(100.milliseconds)
            viewModel.loadAllEpisodes()
        }
    }

    override fun toggleFavorite(episode: Episode) {
        if (favoritesStore.isFavoriteEpisode(episode.id)) {
            favoritesStore.removeFavoriteEpisode(episode.id)
        } else {
            favoritesStore.addFavoriteEpisode(episode.id)
        }
    }

    fun toggleFavoriteOnly() {
        _isFavoritesOnly.update { !it }
        updateFiltered()
    }

    override fun loadNextPage(name: String?) {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(300.milliseconds)
            viewModel.loadNextPage(name)
        }
    }

    override fun loadSearchEpisode(name: String?) {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(300.milliseconds)
            viewModel.refreshWithSearch(name)
        }
    }
}