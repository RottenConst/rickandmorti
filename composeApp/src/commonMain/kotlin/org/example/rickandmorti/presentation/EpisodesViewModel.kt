package org.example.rickandmorti.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.usecase.GetCharacterByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import org.koin.core.component.KoinComponent
import kotlin.collections.orEmpty
import kotlin.collections.plus

class EpisodesViewModel(
    private val getEpisodeUseCase: GetEpisodeUseCase,
    private val getCharacterByUrlUseCase: GetCharacterByUrlUseCase
): BaseViewModel(), KoinComponent {

    private val _stateEpisode = MutableStateFlow<UiStateEpisode>(UiStateEpisode.Loading)
    val stateEpisode: StateFlow<UiStateEpisode> = _stateEpisode
    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private var currentPage = 1
    private var hasMorePages = true
    private var isLoading = false
    private var isLoadingEpisode = false

    init {
        loadAllEpisodes()
    }

    fun loadAllEpisodes(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        if (isLoading || !hasMorePages) return
        isLoading = true

        viewModelScope.launch {
            delay(300)
            when (val result = getEpisodeUseCase(name = query, page = currentPage)){
                is NetworkResult.Success -> {
                    val newEpisodes = result.data
                    if (newEpisodes.isNotEmpty()) {
                        val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                        _stateEpisode.value = UiStateEpisode.Success(currentList + newEpisodes)
                        currentPage++
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
        if (!hasMorePages) return

        val query = name.takeIf { it?.isNotBlank() == true }

        viewModelScope.launch {
            when (val result = getEpisodeUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newEpisodes = result.data
                    val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                    _stateEpisode.value = UiStateEpisode.Success(currentList + newEpisodes)
                    currentPage++
                    hasMorePages = newEpisodes.isNotEmpty()
                }
                is NetworkResult.Error -> {
                    _stateCharacter.value = UiStateCharacter.Error(result.exception.message ?: "Error")
                    hasMorePages = false
                }
            }
        }
    }

    fun refreshWithSearch(name: String? = null) {
        val query = name.takeIf { it?.isNotBlank() == true }
        currentPage = 1
        hasMorePages = true
        _stateEpisode.value = UiStateEpisode.Loading

        loadNextPage(query)
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
                    delay(5000)
                }
            }
            _stateCharacter.value = UiStateCharacter.Success(characters)
            isLoadingEpisode = false
        }
    }

}