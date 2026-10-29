package org.example.rickandmorti.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    private val _currentPage = MutableStateFlow(1)
    private val _isLoading = MutableStateFlow(false)
    private val _isLoadingEpisode = MutableStateFlow(false)

    init {
        observeCache()
        loadedAllCharacters()
    }

    private fun observeCache() {
        viewModelScope.launch {
            getCharactersUseCase.fromCache()
                .catch { e ->
                    Logger.log("Cache observe error: ${e.message}")
                    emit(emptyList())
                }
                .collect { cachedCharacters ->
                    if (_stateCharacter.value is UiStateCharacter.Loading) {
                        if (cachedCharacters.isNotEmpty()) {
                            _stateCharacter.value = UiStateCharacter.Success(
                                cachedCharacters,
                                hasMorePages = true
                            )
                            Logger.log("Loaded ${cachedCharacters.size} from cache as fallback")
                        }
                    }
                }
        }
    }

    fun loadedAllCharacters(name: String? = null){
        val query = name?.takeIf(String::isNotBlank)
        Logger.log("VM Searched for $query")
        viewModelScope.launch {
            loadCharactersPage(query)
        }
    }

    fun loadNextPage(name: String? = null) {
        val currentHasMore = (_stateCharacter.value as? UiStateCharacter.Success)?.hasMorePages ?: false
        if (!currentHasMore || _isLoading.value) return

        val query = name?.takeIf(String::isNotBlank)
        viewModelScope.launch {
            loadCharactersPage(query, isLoadMore = true)
        }
    }

    private suspend fun loadCharactersPage(query: String?, isLoadMore: Boolean = false) {
        if (_isLoading.value) return
        _isLoading.value = true

        try {
            delay(300.milliseconds)

            val page = if (isLoadMore) _currentPage.value else 1
            when (val result = getCharactersUseCase(name = query, page = page)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()
                    val newChars = result.data.filterNot { it.id in currentIds }

                    if (newChars.isNotEmpty()) {
                        val newList = currentList + newChars
                        _stateCharacter.value = UiStateCharacter.Success(
                            newList,
                            hasMorePages = true
                        )
                        _currentPage.value = page + 1
                        Logger.log("VM: loaded ${newChars.size} chars, total: ${newList.size}, next page: ${_currentPage.value}")
                    } else {
                        _stateCharacter.value = UiStateCharacter.Success(
                            currentList,
                            hasMorePages = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    val currentState = _stateCharacter.value as? UiStateCharacter.Success
                    val currentHasMore = currentState?.hasMorePages ?: false

                    _stateCharacter.value = UiStateCharacter.Error(
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
        _currentPage.value = 1
        _stateCharacter.value = UiStateCharacter.Loading
        loadedAllCharacters(query)
    }

    fun loadCharactersByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }
            
            if (idsToLoad.isEmpty()) return@launch
            
            val loadedChars = try {
                coroutineScope {
                    idsToLoad.map { id ->
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
                }
            } catch (e: Exception) {
                Logger.log("Error loading characters by IDs: ${e.message}")
                emptyList()
            }
            
            if (loadedChars.isNotEmpty()) {
                val currentState = _stateCharacter.value as? UiStateCharacter.Success
                _stateCharacter.value = UiStateCharacter.Success(
                    currentList + loadedChars,
                    currentState?.hasMorePages ?: false
                )
                Logger.log("Loaded ${loadedChars.size} missing characters")
            }
        }
    }

    fun loadEpisodes(urls: List<String>) {
        if (_isLoadingEpisode.value) return
        _isLoadingEpisode.value = true
        _stateEpisode.value = UiStateEpisode.Loading

        viewModelScope.launch {
            try {
                val episodes = try {
                    coroutineScope {
                        urls.map { url ->
                            async {
                                when (val result = getEpisodeByUrlUseCase(url)) {
                                    is NetworkResult.Success -> result.data
                                    is NetworkResult.Error -> {
                                        Logger.log("Error loading episode $url: ${result.exception}")
                                        null
                                    }
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                }catch (e: Exception) {
                    Logger.log("Error loading episodes: ${e.message}")
                    emptyList()
                }

                _stateEpisode.value = UiStateEpisode.Success(episodes, false)
            } finally {
                _isLoadingEpisode.value = false
            }
        }
    }
}