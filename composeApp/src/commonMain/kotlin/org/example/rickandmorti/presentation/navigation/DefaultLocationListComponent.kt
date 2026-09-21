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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.presentation.LocationViewModel
import org.example.rickandmorti.presentation.uistate.UiStateLocation
import org.example.rickandmorti.util.Logger

class DefaultLocationListComponent(
    componentContext: ComponentContext,
    private val locationClicked: (Location) -> Unit,
    private val favoritesStore: FavoritesStore,
    private val viewModel: LocationViewModel
): LocationListComponent, ComponentContext by componentContext {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val _allLocations = MutableValue<List<Location>>(emptyList())
    private val _filteredLocations = MutableValue<List<Location>>(emptyList())

    private val _isFavoritesOnly = MutableValue(false)
    override val isFavoritesOnly: Value<Boolean> = _isFavoritesOnly

    override val location: Value<List<Location>> = _filteredLocations
    override val favorites: Flow<Set<Int>> = favoritesStore.favoritesLocationsFlow
    private val _hasMorePages = MutableValue(true)
    override val hasMorePages: Value<Boolean> = _hasMorePages

    private val _isLoadingFavorites = MutableValue(false)
    override val isLoadingFavorites: Value<Boolean> = _isLoadingFavorites

    private var currentFavorites: Set<Int> = emptySet()

    init {
        Logger.log("ListLocationComponent: init started")

        setupLocationFlow()
        setupFavoriteFlow()

        _allLocations.subscribe { updateFiltered() }

        componentContext.lifecycle.doOnCreate {
            Logger.log("Location list component: onCreate")
            viewModel.loadedAllLocations()
        }
        componentContext.lifecycle.doOnDestroy {
            Logger.log("Location list component: onDestroy")
            coroutineScope.cancel()
        }
    }

    private fun setupLocationFlow() {
        coroutineScope.launch {
            viewModel.stateLocation.collect { state ->
                when (state) {
                    is UiStateLocation.Success -> {
                        Logger.log("Received ${state.locations.size} locations from flow")
                        _allLocations.update { state.locations }
//                        updateFiltered()
                        _hasMorePages.value = state.hasMorePages
                    }
                    is UiStateLocation.Loading -> _hasMorePages.value = true
                    is UiStateLocation.Error -> _hasMorePages.value = false
                }
            }
        }
    }

    private fun setupFavoriteFlow() {
        coroutineScope.launch {
            favoritesStore.favoritesLocationsFlow
                .distinctUntilChanged {old, new -> old == new}
                .collect { favorites ->
                    currentFavorites = favorites
                    updateFiltered()
                }
        }
    }

    private fun updateFiltered() {
        val all = _allLocations.value
        val filtered = if (_isFavoritesOnly.value) {
            all.filter { currentFavorites.contains(it.id) }
        } else {
            all
        }
        if (filtered !== _filteredLocations.value) {
            _filteredLocations.value = filtered
            if (_isFavoritesOnly.value && _isLoadingFavorites.value) _isLoadingFavorites.value = false
        }
    }

    override fun toggleFavorite(locations: Location) {
        if (favoritesStore.isFavoriteLocation(locations.id)){
            favoritesStore.removeFavoriteLocation(locations.id)
        } else {
            favoritesStore.addFavoriteLocation(locations.id)
        }
    }

    fun toggleFavoriteOnly() {
        _isFavoritesOnly.update { !it }
        if (isFavoritesOnly.value) {
            coroutineScope.launch { loadingMissingFavorites() }
        } else {
            updateFiltered()
        }
    }

    private fun loadingMissingFavorites() {
        val loadedIds = _allLocations.value.map { it.id }.toSet()
        val missingIds = currentFavorites - loadedIds

        if (missingIds.isNotEmpty()) {
            Logger.log("Missing ${missingIds.size} favorites location, loading...")
            _isLoadingFavorites.value = true
            viewModel.loadedLocationsByIds(missingIds.toList())
        }
        updateFiltered()
    }

    override fun onLocationClick(location: Location) = locationClicked(location)

    override fun loadNextPage() {
        viewModel.loadedAllLocations()
    }

    override fun loadSearchLocation(name: String?) {
        coroutineScope.launch {
            viewModel.refreshWithSearch(name)
        }
    }

    override fun loadNextPage(name: String?) {
        if (!hasMorePages.value) {
            Logger.log("loadNextPage skipped: no more pages")
            return
        }
        viewModel.loadNextPage(name)
    }
}