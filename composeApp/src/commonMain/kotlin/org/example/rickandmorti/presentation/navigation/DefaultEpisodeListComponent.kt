package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.presentation.EpisodesViewModel
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger

class DefaultEpisodeListComponent(
    componentContext: ComponentContext,
    private val episodeClicked: (Episode) -> Unit,
    private val favoritesStore: FavoritesStore,
    private val viewModel: EpisodesViewModel
): EpisodeListComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob())

    private val _allEpisodes = MutableValue<List<Episode>>(emptyList())
    private val _filteredEpisodes = MutableValue<List<Episode>>(emptyList())

    private val _isFavoritesOnly = MutableValue(false)
    override val isFavoritesOnly: Value<Boolean> = _isFavoritesOnly

    private val _hasMorePages = MutableValue(true)
    override val hasMorePages: Value<Boolean> = _hasMorePages

    private val _isLoadingFavorites = MutableValue(false)
    override val isLoadingFavorites: Value<Boolean> = _isLoadingFavorites

    override val episodes: Value<List<Episode>> = _filteredEpisodes
    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesEpisodesFlow
    private var currentFavorites: Set<Int> = emptySet()
    private var loadMoreJob: Job? = null

    init {
        Logger.log("List Episode: init started")

        setupEpisodesFlow()
        setupFavoritesFlow()
        // Обновляем фильтрованный список при изменении всех эпизодов
        _allEpisodes.subscribe { updateFiltered() }

        componentContext.lifecycle.doOnCreate {
            viewModel.loadAllEpisodes()
        }
        componentContext.lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    private fun setupEpisodesFlow() {
        componentScope.launch {
            viewModel.stateEpisode.collect { state ->
                when (state) {
                    is UiStateEpisode.Success -> {
                        Logger.log("Received ${state.episodes.size} episodes from Flow")
                        _allEpisodes.update { state.episodes }
                        _hasMorePages.value = state.hasMorePages
                    }
                    is UiStateEpisode.Loading -> {
                        _hasMorePages.value = true
                    }
                    is UiStateEpisode.Error -> {
                        _hasMorePages.value = false
                    }
                }
            }
        }
    }

    private fun setupFavoritesFlow() {
        componentScope.launch {
            favoritesStore.favoritesEpisodesFlow
                .distinctUntilChanged { old, new -> old == new }
                .collect { favorites ->
                    currentFavorites = favorites
                    updateFiltered()
                }
        }
    }

    private fun updateFiltered() {
        val all = _allEpisodes.value
        val filtered = if (!_isFavoritesOnly.value) {
            all
        } else {
            all.filter { currentFavorites.contains(it.id) }
        }
        if (filtered !== _filteredEpisodes.value) {
            _filteredEpisodes.value = filtered
            // Сброс флага загрузки избранного после обновления фильтрованного списка
            if (_isFavoritesOnly.value && _isLoadingFavorites.value) {
                _isLoadingFavorites.value = false
            }
        }
    }

    override fun onEpisodeClicked(episode: Episode) = episodeClicked(episode)

    override fun loadNextPage() {
        viewModel.loadAllEpisodes()
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
        if (isFavoritesOnly.value) {
            componentScope.launch { loadMissingFavorites() }
        } else {
            updateFiltered()
        }
    }

    private fun loadMissingFavorites() {
        val loadedIds = _allEpisodes.value.map { it.id }.toSet()
        val missingIds = currentFavorites - loadedIds
        
        if (missingIds.isNotEmpty()) {
            Logger.log("Missing ${missingIds.size} favorites episodes, loading...")
            _isLoadingFavorites.value = true
            viewModel.loadEpisodesByIds(missingIds.toList())
        }
        updateFiltered()
    }

    override fun loadNextPage(name: String?) {
        if (!hasMorePages.value) {
            Logger.log("loadNextPage skipped: no more pages")
            return
        }
        viewModel.loadNextPage(name)
    }

    override fun loadSearchEpisode(name: String?) {
        componentScope.launch {
            viewModel.refreshWithSearch(name)
        }
    }
}