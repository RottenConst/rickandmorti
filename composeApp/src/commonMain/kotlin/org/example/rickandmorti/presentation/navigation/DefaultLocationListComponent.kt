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
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.presentation.LocationViewModel
import org.example.rickandmorti.presentation.uistate.UiStateLocation
import org.example.rickandmorti.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

class DefaultLocationListComponent(
    componentContext: ComponentContext,
    private val locationClicked: (Location) -> Unit,
    private val favoritesStore: FavoritesStore
): LocationListComponent, ComponentContext by componentContext, KoinComponent {
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null
    private val viewModel: LocationViewModel = get<LocationViewModel> {
        parametersOf()
    }

    init {
        Logger.log("ListLocationComponent: init started")

        favoritesStore.favoritesLocationsFlow
            .distinctUntilChanged()
            .onEach { favorites ->
                currentFavorites = favorites
                updateFiltered()
            }.launchIn(scope)

        viewModel.stateLocation
            .onEach { uiLocationState ->
                when(uiLocationState) {
                    is UiStateLocation.Success -> {
                        Logger.log("Received ${uiLocationState.locations.size} locations from flow")
                        _allLocations.update { uiLocationState.locations }
                        updateFiltered()
                        _hasMorePages.value = viewModel.hasMorePages
                    }
                    is UiStateLocation.Loading -> {
                        _hasMorePages.value = true
                    }
                    is UiStateLocation.Error -> {
                        _hasMorePages.value = false
                    }
                }
            }.launchIn(scope)

        lifecycle.subscribe(
            onCreate = {
                Logger.log("ListLocationComponent: onCreate")
                reloadLocations()
                _hasMorePages.value = true
            },
            onDestroy = {
                Logger.log("ListLocationComponent: destroyed")
                scope.cancel()
            }
        )
    }

    private fun updateFiltered() {
        val all = _allLocations.value
        val isFavoriteOnly = _isFavoritesOnly.value
        val filtered = if (isFavoriteOnly) {
            all.filter { currentFavorites.contains(it.id) }
        } else {
            all
        }
        _filteredLocations.value = filtered
        if (isFavoriteOnly && _isLoadingFavorites.value) _isLoadingFavorites.value = false
    }

    private fun reloadLocations(name: String? = null) {
        viewModel.loadedAllLocations(name)
    }

    override fun toggleFavorite(locations: Location) {
        if (favoritesStore.isFavoriteLocation(locations.id)){
            favoritesStore.removeFavoriteLocation(locations.id)
        }
        else {
            favoritesStore.addFavoriteLocation(locations.id)
        }
    }

    fun toggleFavoriteOnly() {
        _isFavoritesOnly.update { !it }
        if (isFavoritesOnly.value) {
            loadingMissingFavorites()
        } else {
            updateFiltered()
        }
    }

    private fun loadingMissingFavorites() {
        val favorites = currentFavorites
        val loadedIds = _allLocations.value.map { it.id }.toSet()
        val missingIds = favorites - loadedIds

        if (missingIds.isNotEmpty()) {
            Logger.log("Missing ${missingIds.size} favorites location, loading...")
            _isLoadingFavorites.value = true
            viewModel.loadedLocationsByIds(missingIds.toList())
        } else {
            updateFiltered()
        }
        updateFiltered()
    }

    override fun onLocationClick(location: Location) = locationClicked(location)

    override fun loadNextPage() {
        reloadLocations()
    }

    override fun loadSearchLocation(name: String?) {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(300.milliseconds)
            viewModel.refreshWithSearch(name)
        }
    }

    override fun loadNextPage(name: String?) {
        viewModel.loadNextPage(name)
    }
}