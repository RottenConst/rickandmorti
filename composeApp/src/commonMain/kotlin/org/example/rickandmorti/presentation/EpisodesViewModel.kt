package org.example.rickandmorti.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.usecase.GetCharacterByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.milliseconds

class EpisodesViewModel(
    private val getEpisodeUseCase: GetEpisodeUseCase,
    private val getCharacterByUrlUseCase: GetCharacterByUrlUseCase
): BaseViewModel(), KoinComponent {

    private val _stateEpisode = MutableStateFlow<UiStateEpisode>(UiStateEpisode.Loading)
    val stateEpisode: StateFlow<UiStateEpisode> = _stateEpisode
    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private var currentPage = 1
    private var isLoading = false
    private var isLoadingEpisode = false
    var hasMorePages = true

    init {
        loadAllEpisodes()
    }

    fun loadAllEpisodes(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        if (isLoading || !hasMorePages) return
        isLoading = true

        viewModelScope.launch {
            delay(300.milliseconds)
            when (val result = getEpisodeUseCase(name = query, page = currentPage)){
                is NetworkResult.Success -> {
                    val newEpisodes = result.data
                    if (newEpisodes.isNotEmpty()) {
                        val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                        val uniqueNewEpisodes = newEpisodes.filterNot { existingEp ->
                            currentList.any { it.id == existingEp.id }
                        }
                        val updatedList = currentList + uniqueNewEpisodes
                        _stateEpisode.value = UiStateEpisode.Success(updatedList)
                        currentPage++
                        hasMorePages = newEpisodes.isNotEmpty()
                        Logger.log(message = "VM: loaded ${newEpisodes.size} episods, total: ${currentList.size + newEpisodes.size}")
                    } else {
                        hasMorePages = false
                    }
                }

                is NetworkResult.Error -> {
                    _stateEpisode.value = UiStateEpisode.Error(result.exception.message ?: "Unknown error")
                    hasMorePages = false
                    Logger.log("VM Error: ${result.exception.message}")
                }
            }
            isLoading = false
        }
    }

    fun loadNextPage(name: String? = null) {
        Logger.log("loadNextPage called, hasMorePages: $hasMorePages, currentPage: $currentPage")
        if (!hasMorePages) return

        val query = name.takeIf { it?.isNotBlank() == true }

        viewModelScope.launch {
            when (val result = getEpisodeUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newEpisodes = result.data
                    val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                    val uniqueNewEpisodes = newEpisodes.filterNot { ep ->
                        currentList.any { it.id == ep.id }
                    }
                    val updatedList = currentList + uniqueNewEpisodes
                    _stateEpisode.value = UiStateEpisode.Success(updatedList)
                    currentPage++
                    hasMorePages = newEpisodes.isNotEmpty()
                }
                is NetworkResult.Error -> {
                    _stateEpisode.value = UiStateEpisode.Error(result.exception.message ?: "Error")
                    hasMorePages = false
                    Logger.log("loadNextPage error: ${result.exception.message}")
                }
            }
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        currentPage = 1
        hasMorePages = true
        _stateEpisode.value = UiStateEpisode.Loading

        loadAllEpisodes(query)
    }

    fun loadEpisodesByIds(ids: List<Int>) {
        viewModelScope.launch {
            val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
            val currentIds = currentList.map { it.id }.toSet()
            val idsToLoad = ids.filterNot { it in currentIds }
            
            if (idsToLoad.isEmpty()) return@launch
            
            val loadedEpisodes = mutableListOf<Episode>()
            for (id in idsToLoad) {
                when (val result = getEpisodeUseCase(id = id)) {
                    is NetworkResult.Success -> {
                        loadedEpisodes.add(result.data)
                    }
                    is NetworkResult.Error -> {
                        Logger.log("Error loading episode $id: ${result.exception}")
                    }
                }
            }
            
            if (loadedEpisodes.isNotEmpty()) {
                val currentUpdatedList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                val updatedList = currentUpdatedList + loadedEpisodes
                _stateEpisode.value = UiStateEpisode.Success(updatedList)
                Logger.log("Loaded ${loadedEpisodes.size} missing episodes")
            }
            hasMorePages = false
        }
    }

    fun loadedCharacter(urls: List<String>) {
        if (isLoadingEpisode) return
        isLoadingEpisode = true
        _stateCharacter.value = UiStateCharacter.Loading

        viewModelScope.launch {
            val characters = mutableListOf<Character>()
            for ((index, url) in urls.withIndex()) {
                when (val result = getCharacterByUrlUseCase(url)) {
                    is NetworkResult.Success -> characters.add(result.data)
                    is NetworkResult.Error -> {}
                }
                if (index + 1 % 10 == 0) {
                    delay(5000.milliseconds)
                }
            }
            _stateCharacter.value = UiStateCharacter.Success(characters)
            isLoadingEpisode = false
        }
    }

}