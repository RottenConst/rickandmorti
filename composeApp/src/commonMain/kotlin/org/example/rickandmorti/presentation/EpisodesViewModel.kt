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
import org.example.rickandmorti.domain.usecase.GetEpisodeUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import kotlin.time.Duration.Companion.milliseconds

class EpisodesViewModel(
    private val getEpisodeUseCase: GetEpisodeUseCase,
    private val getCharacterByUrlUseCase: GetCharacterByUrlUseCase
): ViewModel() {

    private val _stateEpisode = MutableStateFlow<UiStateEpisode>(UiStateEpisode.Loading)
    val stateEpisode: StateFlow<UiStateEpisode> = _stateEpisode
    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private var _currentPage = MutableStateFlow(1)
    private var _isLoading = MutableStateFlow(false)
    private var _isLoadingEpisode = MutableStateFlow(false)

    init {
        observeCache()
        loadAllEpisodes()
    }

    private fun observeCache() {
        viewModelScope.launch {
            getEpisodeUseCase.fromCache()
                .catch { e ->
                    Logger.log("Cache observe error: ${e.message}")
                    emit(emptyList())
                }
                .collect { cachedEpisodes ->
                    if (_stateEpisode.value is UiStateEpisode.Loading) {
                        if (cachedEpisodes.isNotEmpty()) {
                            _stateEpisode.value = UiStateEpisode.Success(
                                cachedEpisodes,
                                hasMorePages = true
                            )
                            Logger.log("Loaded ${cachedEpisodes.size} from cache as fallback")
                        }
                    }
                }
        }
    }

    fun loadAllEpisodes(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        viewModelScope.launch {
            getEpisodesPage(query)
        }
    }

    fun loadNextPage(name: String? = null) {
        val currentHasMore = (_stateEpisode.value as? UiStateEpisode.Success)?.hasMorePages ?: false
        if (!currentHasMore || _isLoading.value) return

        val query = name.takeIf { it?.isNotBlank() == true }
        viewModelScope.launch {
            getEpisodesPage(query, isLoadMore = true)
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        _currentPage.value = 1
        _stateEpisode.value = UiStateEpisode.Loading
        loadAllEpisodes(query)
    }

    fun loadEpisodesByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }
            
            if (idsToLoad.isEmpty()) return@launch

            val loadedEpisodes = try {
                coroutineScope {
                    idsToLoad.map { id ->
                        async {
                            when (val result = getEpisodeUseCase(id)) {
                                is NetworkResult.Success -> result.data
                                is NetworkResult.Error -> {
                                    Logger.log("Error loading episode $id: ${result.exception}")
                                    null
                                }
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
            } catch (e: Exception) {
                Logger.log("Error loading episodes by IDs: ${e.message}")
                emptyList()
            }
            
            if (loadedEpisodes.isNotEmpty()) {
                val currentState = _stateEpisode.value as? UiStateEpisode.Success
                _stateEpisode.value = UiStateEpisode.Success(
                    currentList + loadedEpisodes,
                    currentState?.hasMorePages ?: false
                )
                Logger.log("Loaded ${loadedEpisodes.size} missing episodes")
            }
        }
    }

    private suspend fun getEpisodesPage(query: String?, isLoadMore: Boolean = false) {
        if (_isLoading.value) return
        _isLoading.value = true

        return try {
            delay(300.milliseconds)
            val page = if (isLoadMore) _currentPage.value else 1
            when (val result = getEpisodeUseCase(name = query, page = page)) {
                is NetworkResult.Success -> {
                    val currentEpisodes = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                    val currentIds = currentEpisodes.map { it.id }.toSet()
                    val newEpisodes = result.data.filterNot { it.id in currentIds }

                    if (newEpisodes.isNotEmpty()) {
                        val newList = currentEpisodes + newEpisodes
                        _stateEpisode.value = UiStateEpisode.Success(
                            newList,
                            hasMorePages = true
                        )
                        _currentPage.value = page + 1
                        Logger.log("VM: loaded ${newEpisodes.size} episodes, total: ${newList.size}, next page: ${_currentPage.value}")
                    } else {
                        _stateEpisode.value = UiStateEpisode.Success(
                            currentEpisodes,
                            hasMorePages = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    val currentState = _stateEpisode.value as? UiStateEpisode.Success
                    val currentHasMore = currentState?.hasMorePages ?: false

                    _stateEpisode.value = UiStateEpisode.Error(
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

    fun loadedCharacter(urls: List<String>) {
        if (_isLoadingEpisode.value) return
        _isLoadingEpisode.value = true
        _stateCharacter.value = UiStateCharacter.Loading

        viewModelScope.launch {
            val characters = urls.map { url ->
                async {
                    when (val result = getCharacterByUrlUseCase(url)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> null
                    }
                }
            }.awaitAll().filterNotNull()
            _stateCharacter.value = UiStateCharacter.Success(characters,false)
            _isLoadingEpisode.value = false
        }
    }

}