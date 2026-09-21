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

    private var currentPage = 1
    private var isLoading = false
    private var isLoadingEpisode = false

    init {
        loadAllEpisodes()
    }

    fun loadAllEpisodes(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            delay(300.milliseconds)
            when (val result = getEpisodeUseCase(name = query, page = currentPage)){
                is NetworkResult.Success -> {
                    val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                    val newEpisodes = result.data

                    val hasMore = newEpisodes.isNotEmpty()
                    if (hasMore) {
                        val uniqueNewEpisodes = newEpisodes.filterNot { existingEp ->
                            currentList.any { it.id == existingEp.id }
                        }
                        val updatedList = currentList + uniqueNewEpisodes
                        _stateEpisode.value = UiStateEpisode.Success(updatedList, hasMorePages = true)
                        currentPage++
                        Logger.log(message = "VM: loaded ${newEpisodes.size} episods, total: ${currentList.size + newEpisodes.size}")
                    } else {
                        _stateEpisode.value = UiStateEpisode.Success(currentList, hasMorePages = false)
                    }
                }

                is NetworkResult.Error -> {
                    val currentHasMore = (_stateEpisode.value as? UiStateEpisode.Success)?.hasMorePages ?: false
                    _stateEpisode.value = UiStateEpisode.Error(
                        message = result.exception.message ?: "Unknown error",
                        hasMorePages = currentHasMore
                    )
                    Logger.log("VM Error: ${result.exception.message}")
                }
            }
            isLoading = false
        }
    }

    fun loadNextPage(name: String? = null) {
        val currentHasMore = (_stateEpisode.value as? UiStateEpisode.Success)?.hasMorePages ?: false
        if (!currentHasMore || isLoading) return

        val query = name.takeIf { it?.isNotBlank() == true }

        viewModelScope.launch {
            when (val result = getEpisodeUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()
                    val newEpisodes = result.data.filterNot { it.id in currentIds }

                    val hasMore = newEpisodes.isNotEmpty()
                    if (hasMore) {
                        _stateEpisode.value = UiStateEpisode.Success(currentList + newEpisodes, hasMorePages = true)
                        currentPage++
                    } else _stateEpisode.value = UiStateEpisode.Success(currentList, hasMorePages = false)

                }
                is NetworkResult.Error -> {
                    _stateEpisode.value = UiStateEpisode.Error(
                        message = result.exception.message ?: "Error",
                        hasMorePages = false
                    )
                    Logger.log("loadNextPage error: ${result.exception.message}")
                }
            }
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        currentPage = 1
        _stateEpisode.value = UiStateEpisode.Loading
        loadAllEpisodes(query)
    }

    fun loadEpisodesByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentState = _stateEpisode.value as? UiStateEpisode.Success
            val currentList = currentState?.episodes.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }
            
            if (idsToLoad.isEmpty()) return@launch
            
            val loadedEpisodes = idsToLoad.map { id ->
                async {
                    when (val result = getEpisodeUseCase(id = id)) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> {
                            Logger.log("Error loading episode $id: ${result.exception}")
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
            
            if (loadedEpisodes.isNotEmpty()) {
                _stateEpisode.value = UiStateEpisode.Success(currentList + loadedEpisodes, currentState?.hasMorePages ?: false)
                Logger.log("Loaded ${loadedEpisodes.size} missing episodes")
            }
        }
    }

    fun loadedCharacter(urls: List<String>) {
        if (isLoadingEpisode) return
        isLoadingEpisode = true
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
            isLoadingEpisode = false
        }
    }

}