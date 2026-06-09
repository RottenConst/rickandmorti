package org.example.rickandmorti.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.usecase.GetCharacterByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetLocationsUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateLocation
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.milliseconds

class LocationViewModel(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getCharacterByUrlUseCase: GetCharacterByUrlUseCase
): BaseViewModel(), KoinComponent {
    private val _stateLocations = MutableStateFlow<UiStateLocation>(UiStateLocation.Loading)
    val stateLocation: StateFlow<UiStateLocation> = _stateLocations

    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private var currentPage = 1
    private var isLoading = false
    private var isLoadingCharacters = false
    private var hasMorePages = true

    init {
        loadedAllLocations()
    }

    fun loadedAllLocations(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        if (isLoading || !hasMorePages) return
        isLoading = true

        viewModelScope.launch {
            delay(300.milliseconds)
            when(val result = getLocationsUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newLocation = result.data
                    if (newLocation.isNotEmpty()) {
                        val currentList = (_stateLocations.value as? UiStateLocation.Success)?.locations.orEmpty()
                        val uniqueNewLocation = newLocation.filterNot { existingLocation ->
                            currentList.any {it.id == existingLocation.id}
                        }
                        val updatedList = currentList + uniqueNewLocation
                        _stateLocations.value = UiStateLocation.Success(updatedList)
                        currentPage++
                        Logger.log("VM Loaded ${newLocation.size} location, total ${currentList.size + newLocation.size}")
                    } else {
                        hasMorePages = false
                    }
                }
                is NetworkResult.Error -> {
                    _stateLocations.value = UiStateLocation.Error(result.exception.message ?: "unknown error")
                    hasMorePages = false
                    Logger.log("VM Error: ${result.exception.message}")
                }
            }
            isLoading = false
        }
    }

    fun loadNextPage(name: String? = null) {
        if (!hasMorePages) return

        val query = name.takeIf { it?.isNotBlank() == true }

        viewModelScope.launch {
            when(val result = getLocationsUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newLocation = result.data
                    val currentList = (_stateLocations.value as? UiStateLocation.Success)?.locations.orEmpty()
                    val uniqueNewLocation = newLocation.filterNot { location ->
                        currentList.any { it.id == location.id }
                    }
                    val updatedNewLocation = currentList + uniqueNewLocation
                    _stateLocations.value = UiStateLocation.Success(updatedNewLocation)
                    currentPage++
                    hasMorePages = newLocation.isNotEmpty()
                }

                is NetworkResult.Error -> {
                    _stateLocations.value = UiStateLocation.Error(result.exception.message ?: "Unknown error")
                    hasMorePages = false
                }
            }
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        currentPage = 1
        hasMorePages = true
        _stateLocations.value = UiStateLocation.Loading

        loadedAllLocations(query)
    }

    fun loadResidents(urls: List<String>) {
        if (isLoadingCharacters) return
        isLoadingCharacters = true
        _stateCharacter.value = UiStateCharacter.Loading

        viewModelScope.launch {
            val character = mutableListOf<Character>()
            for ((index, url) in urls.withIndex()) {
                when (val result = getCharacterByUrlUseCase(url)) {
                    is NetworkResult.Success -> character.add(result.data)
                    is NetworkResult.Error -> {}
                }
                if (index + 1 % 10 == 0) {
                    delay(5000.milliseconds)
                }
            }
            _stateCharacter.value = UiStateCharacter.Success(character)
            isLoadingCharacters = false
        }
    }
}