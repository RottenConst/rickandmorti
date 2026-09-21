package org.example.rickandmorti.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.usecase.GetCharacterByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetLocationsUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateLocation
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import kotlin.time.Duration.Companion.milliseconds

class LocationViewModel(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getCharacterByUrlUseCase: GetCharacterByUrlUseCase
): ViewModel() {
    private val _stateLocations = MutableStateFlow<UiStateLocation>(UiStateLocation.Loading)
    val stateLocation: StateFlow<UiStateLocation> = _stateLocations

    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private var currentPage = 1
    private var isLoading = false
    private var isLoadingCharacters = false

    init {
        loadedAllLocations()
    }

    fun loadedAllLocations(name: String? = null) {
        val query = name?.takeIf(String::isNotBlank)
        Logger.log("VM Searched for $query")
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            delay(300.milliseconds)
            when(val result = getLocationsUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateLocations.value as? UiStateLocation.Success)?.locations.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()
                    val newLocation = result.data.filterNot { it.id in currentIds }

                    val hasMore = newLocation.isNotEmpty()
                    if (hasMore) {
                        _stateLocations.value = UiStateLocation.Success(currentList + newLocation, hasMorePages = true)
                        currentPage++
                        Logger.log("VM Loaded ${newLocation.size} location, total ${currentList.size + newLocation.size}")
                    } else {
                        _stateLocations.value = UiStateLocation.Success(currentList, hasMorePages = false)
                    }
                }
                is NetworkResult.Error -> {
                    val currentHasMore = (_stateLocations.value as? UiStateLocation.Success)?.hasMorePages ?: false
                    _stateLocations.value = UiStateLocation.Error(
                        message = result.exception.message ?: "unknown error",
                        hasMorePages = currentHasMore
                    )
                    Logger.log("VM Error: ${result.exception.message}")
                }
            }
            isLoading = false
        }
    }

    fun loadNextPage(name: String? = null) {
        val currentHasMore = (_stateLocations.value as? UiStateLocation.Success)?.hasMorePages ?: false
        if (!currentHasMore || isLoading) return

        val query = name?.takeIf(String::isNotBlank)

        viewModelScope.launch {
            when(val result = getLocationsUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateLocations.value as? UiStateLocation.Success)?.locations.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()
                    val newLocation = result.data.filterNot { it.id in currentIds }

                    val hasMore = newLocation.isNotEmpty()
                    if (hasMore) {
                        _stateLocations.value = UiStateLocation.Success(currentList + newLocation, hasMorePages = true)
                        currentPage++
                    } else _stateLocations.value = UiStateLocation.Success(currentList, hasMorePages = false)

                }

                is NetworkResult.Error -> {
                    _stateLocations.value = UiStateLocation.Error(
                        message = result.exception.message ?: "Unknown error",
                        hasMorePages = false
                    )
                }
            }
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        currentPage = 1
        _stateLocations.value = UiStateLocation.Loading
        loadedAllLocations(query)
    }

    fun loadedLocationsByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentState = _stateLocations.value as? UiStateLocation.Success
            val currentList = currentState?.locations.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }

            if (idsToLoad.isEmpty()) return@launch

            val loadedLocation = idsToLoad.map { id ->
                async {
                    when (val result = getLocationsUseCase(id)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> {
                            Logger.log("Error loading location $id: ${result.exception}")
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()

            if (loadedLocation.isNotEmpty()) {
                _stateLocations.value = UiStateLocation.Success(currentList + loadedLocation, currentState?.hasMorePages ?: false)
                Logger.log("Loaded ${loadedLocation.size} missing locations")
            }
        }
    }

    fun loadResidents(urls: List<String>) {
        if (isLoadingCharacters) return
        isLoadingCharacters = true
        _stateCharacter.value = UiStateCharacter.Loading

        viewModelScope.launch {
            val character = urls.map { url ->
                async {
                    when (val result = getCharacterByUrlUseCase(url)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll().filterNotNull()

            _stateCharacter.value = UiStateCharacter.Success(character, false)
            isLoadingCharacters = false
        }
    }
}