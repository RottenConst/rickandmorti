package org.example.rickandmorti.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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

    private var _currentPage = MutableStateFlow(1)
    private var _isLoading = MutableStateFlow(false)
    private var _isLoadingCharacters = MutableStateFlow(false)

    init {
        observeCache()
        loadedAllLocations()
    }

    private fun observeCache() {
        viewModelScope.launch {
            getLocationsUseCase.fromCache()
                .catch { e ->
                    Logger.log("Cache observe error: ${e.message}")
                    emit(emptyList())
                }
                .collect { cachedLocations ->
                    if (_stateLocations.value is UiStateLocation.Loading) {
                        if (cachedLocations.isNotEmpty()) {
                            _stateLocations.value = UiStateLocation.Success(
                                cachedLocations,
                                hasMorePages = false
                            )
                            Logger.log("Loaded ${cachedLocations.size} from cache as fallback")
                        }
                    }
                }
        }
    }

    fun loadedAllLocations(name: String? = null) {
        val query = name?.takeIf(String::isNotBlank)
        Logger.log("VM Searched for $query")
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            getLocationPage(query)
        }
    }

    fun loadNextPage(name: String? = null) {
        val currentHasMore = (_stateLocations.value as? UiStateLocation.Success)?.hasMorePages ?: false
        if (!currentHasMore || _isLoading.value) return

        val query = name?.takeIf(String::isNotBlank)

        viewModelScope.launch {
            getLocationPage(query, isLoadMore = true)
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        _currentPage.value = 1
        _stateLocations.value = UiStateLocation.Loading
        loadedAllLocations(query)
    }

    fun loadedLocationsByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentList = (_stateLocations.value as? UiStateLocation.Success)?.locations.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }

            if (idsToLoad.isEmpty()) return@launch

            val loadedLocation = try {
                coroutineScope {
                    idsToLoad.map { id ->
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
                }
            } catch (e: Exception) {
                Logger.log("Error loading locations by IDs: ${e.message}")
                emptyList()
            }

            if (loadedLocation.isNotEmpty()) {
                val currentState = _stateLocations.value as? UiStateLocation.Success
                _stateLocations.value = UiStateLocation.Success(
                    currentList + loadedLocation,
                    currentState?.hasMorePages ?: false
                )
                Logger.log("Loaded ${loadedLocation.size} missing locations")
            }
        }
    }

    private suspend fun getLocationPage(query: String?, isLoadMore: Boolean = false) {
        if (_isLoading.value) return
        _isLoading.value = true

        return try {
            delay(300.milliseconds)
            val page = if (isLoadMore) _currentPage.value else 1
            when (val result = getLocationsUseCase(name = query, page = page)) {
                is NetworkResult.Success -> {
                    val currentLocations = (_stateLocations.value as? UiStateLocation.Success)?.locations.orEmpty()
                    val currentIds = currentLocations.map { it.id }.toSet()
                    val newLocations = result.data.filterNot { it.id in currentIds }

                    if (newLocations.isNotEmpty()) {
                        val newList = currentLocations + newLocations
                        _stateLocations.value = UiStateLocation.Success(
                            newList,
                            hasMorePages = true
                        )
                        _currentPage.value = page + 1
                        Logger.log("VM: loaded ${newLocations.size} locations, total: ${newList.size}, next page: ${_currentPage.value}")
                    } else {
                        _stateLocations.value = UiStateLocation.Success(
                            currentLocations,
                            hasMorePages = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    val currentState = _stateLocations.value as? UiStateLocation.Success
                    val currentHasMore = currentState?.hasMorePages ?: false

                    _stateLocations.value = UiStateLocation.Error(
                        message = result.exception.message ?: "Unknown error",
                        hasMorePages = currentHasMore
                    )
                    Logger.log("VM Error: ${result.exception}")
                }
            }
        } finally {
            _isLoading.value = false
        }
    }

    fun loadResidents(urls: List<String>) {
        if (_isLoadingCharacters.value) return
        _isLoadingCharacters.value = true
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
            _isLoadingCharacters.value = false
        }
    }
}