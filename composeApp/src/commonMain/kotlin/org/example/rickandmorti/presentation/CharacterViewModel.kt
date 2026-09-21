package org.example.rickandmorti.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetLocationByUrlUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import kotlin.time.Duration.Companion.milliseconds

class CharacterViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getEpisodeByUrlUseCase: GetEpisodeByUrlUseCase,
    private val getLocationByUrlUseCase: GetLocationByUrlUseCase
) : ViewModel() {
    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter.asStateFlow()

    private val _stateEpisode = MutableStateFlow<UiStateEpisode>(UiStateEpisode.Loading)
    val stateEpisode: StateFlow<UiStateEpisode> = _stateEpisode.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _origin = MutableStateFlow<Location?>(null)

    private var currentPage = 1
    private var isLoading = false
    private var isLoadingEpisode = false

    init {
        loadedAllCharacters()
    }

    fun loadedAllCharacters(name: String? = null){
        val query = name?.takeIf(String::isNotBlank)
        Logger.log("VM Searched for $query")

        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            delay(300.milliseconds)

            when (val result = getCharactersUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()
                    val newChars = result.data.filterNot { it.id in currentIds }

                    val hasMore = newChars.isNotEmpty()
                    if (hasMore) {
                        _stateCharacter.value = UiStateCharacter.Success(currentList + newChars, hasMorePages = true)
                        currentPage++
                        Logger.log(message = "VM: loaded ${newChars.size} chars, total: ${currentList.size + newChars.size}")
                    } else {
                        _stateCharacter.value = UiStateCharacter.Success(currentList, hasMorePages = false)
                    }
                }
                is NetworkResult.Error -> {
                    val currentState = _stateCharacter.value as? UiStateCharacter.Success
                    val currentHasMore = currentState?.hasMorePages ?: false

                    if (currentState != null) {
                        _stateCharacter.value = UiStateCharacter.Error(
                            result.exception.message ?: "Unknown error",
                            hasMorePages = currentHasMore
                        )
                    } else {
                        _stateCharacter.value = UiStateCharacter.Error(
                            message = result.exception.message ?: "Unknown error",
                            hasMorePages = false
                        )
                    }

                    Logger.log("VM Error: ${result.exception}")
                }
            }
            isLoading = false
        }
    }

    fun loadNextPage(name: String? = null) {
        val currentHasMore = (_stateCharacter.value as? UiStateCharacter.Success)?.hasMorePages ?: false
        if (!currentHasMore || isLoading) return

        val query = name?.takeIf(String::isNotBlank)
        viewModelScope.launch {
            delay(300.milliseconds)
            when (val result = getCharactersUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()
                    val newChars = result.data.filterNot { it.id in currentIds }

                    val hasMore = newChars.isNotEmpty()
                    if (hasMore) {
                        _stateCharacter.value = UiStateCharacter.Success(currentList + newChars, hasMorePages = true)
                        currentPage++
                    } else {
                        _stateCharacter.value = UiStateCharacter.Success(currentList, hasMorePages = false)
                    }
                }
                is NetworkResult.Error -> {
                    val currentState = _stateCharacter.value as? UiStateCharacter.Success
                    _stateCharacter.value = UiStateCharacter.Error(
                        message = result.exception.message ?: "Error",
                        hasMorePages = currentState?.hasMorePages ?: false
                    )
                    Logger.log("VM Error loadNextPage: ${result.exception}")
                }
            }
        }
    }

    fun getLocation(locationUrl: String, isOrigin: Boolean) {
        viewModelScope.launch {
            when (val result = getLocationByUrlUseCase(locationUrl)) {
                is NetworkResult.Success -> {
                    if (isOrigin) _origin.value = result.data else _location.value = result.data
                }
                is NetworkResult.Error -> Logger.log("Error loading location by url: ${result.exception}")
            }
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        currentPage = 1
        _stateCharacter.value = UiStateCharacter.Loading
        loadedAllCharacters(query)
    }

    fun loadCharactersByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentState = _stateCharacter.value as? UiStateCharacter.Success
            val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }
            
            if (idsToLoad.isEmpty()) return@launch
            
            val loadedChars = idsToLoad.map { id ->
                async {
                    when (val result = getCharactersUseCase(id = id)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> {
                            Logger.log("Error loading character $id: ${result.exception}")
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
            
            if (loadedChars.isNotEmpty()) {
                _stateCharacter.value = UiStateCharacter.Success(currentList + loadedChars, currentState?.hasMorePages ?: false)
                Logger.log("Loaded ${loadedChars.size} missing characters")
            }
        }
    }

    fun loadEpisodes(urls: List<String>) {
        if (isLoadingEpisode) return
        isLoadingEpisode = true
        _stateEpisode.value = UiStateEpisode.Loading

        viewModelScope.launch {
            val episodes = urls.map { url ->
                async {
                    when (val result = getEpisodeByUrlUseCase(url)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll().filterNotNull()

            _stateEpisode.value = UiStateEpisode.Success(episodes, false)
            isLoadingEpisode = false
        }
    }
}