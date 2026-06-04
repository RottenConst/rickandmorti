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
): LocationListComponent, ComponentContext by componentContext, KoinComponent {
    private val _allLocations = MutableValue<List<Location>>(emptyList())
    override val location: Value<List<Location>> = _allLocations
    private val _hasMorePages = MutableValue(true)
    override val hasMorePages: Value<Boolean> = _hasMorePages

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadMoreJob: Job? = null

    private val viewModel: LocationViewModel = get<LocationViewModel> {
        parametersOf()
    }

    init {
        Logger.log("ListLocationComponent: init started")

        viewModel.stateLocation
            .onEach { location ->
                when(location) {
                    is UiStateLocation.Success -> {
                        Logger.log("Received ${location.locations.size} locations from flow")
                        _allLocations.update { location.locations }
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
                scope.launch {
                    viewModel.loadedAllLocations()
                }
            },
            onDestroy = {
                Logger.log("ListLocationComponent: destroyed")
                scope.cancel()
            }
        )
    }

    override fun onLocationClick(location: Location) = locationClicked(location)

    override fun loadNextPage() {
        loadMoreJob?.cancel()
        loadMoreJob = scope.launch {
            delay(100.milliseconds)
            viewModel.loadedAllLocations()
        }
    }

    override fun loadSearchLocation(name: String?) {
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