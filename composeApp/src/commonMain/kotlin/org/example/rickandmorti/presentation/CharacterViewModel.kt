package org.example.rickandmorti.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.usecase.GetCharacterByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeUseCase
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import org.koin.core.component.KoinComponent

class CharacterViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getEpisodeByUrlUseCase: GetEpisodeByUrlUseCase,
    private val getEpisodeUseCase: GetEpisodeUseCase,
    private val getCharacterByUrlUseCase: GetCharacterByUrlUseCase
) : BaseViewModel(), KoinComponent {
    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private val _stateEpisode = MutableStateFlow<UiStateEpisode>(UiStateEpisode.Loading)
    val stateEpisode: StateFlow<UiStateEpisode> = _stateEpisode

    private var currentPage = 1
    private var currentPageEpisode = 1
    private var isLoading = false

    private var isLoadingEpisode = false
    private var hasMorePages = true

    init {
        loadedAllCharacters()
    }

    fun loadedAllCharacters(name: String? = null){
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        if (isLoading || !hasMorePages) return
        isLoading = true

        viewModelScope.launch {
            delay(300)

            when (val result = getCharactersUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newChars = result.data
                    if (newChars.isNotEmpty()) {
                        val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                        _stateCharacter.value = UiStateCharacter.Success(currentList + newChars)
                        currentPage++
                        Logger.log(message = "VM: loaded ${newChars.size} chars, total: ${currentList.size + newChars.size}")
                    } else {
                        hasMorePages = false
                    }
                }
                is NetworkResult.Error -> {
                    _stateCharacter.value = UiStateCharacter.Error(result.exception.message ?: "Unknown error")
                    hasMorePages = false
                    Logger.log("VM Error: ${result.exception}")
                }
            }
            isLoading = false
        }
    }

    fun loadNextPage(name: String? = null) {
        if (!hasMorePages) return

        val query = name.takeIf { it?.isNotBlank() == true }

        viewModelScope.launch {
            when (val result = getCharactersUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newChars = result.data
                    val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                    _stateCharacter.value = UiStateCharacter.Success(currentList + newChars)
                    currentPage++
                    hasMorePages = newChars.isNotEmpty()
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
        _stateCharacter.value = UiStateCharacter.Loading

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

    fun loadAllEpisodes() {
        if (isLoading || !hasMorePages) return
        isLoading = true

        viewModelScope.launch {
            delay(300)
            when (val result = getEpisodeUseCase(currentPageEpisode)){
                is NetworkResult.Success -> {
                    val newEpisodes = result.data
                    if (newEpisodes.isNotEmpty()) {
                        val currentList = (_stateEpisode.value as? UiStateEpisode.Success)?.episodes.orEmpty()
                        _stateEpisode.value = UiStateEpisode.Success(currentList + newEpisodes)
                        currentPageEpisode++
                        Logger.log(message = "VM: loaded ${newEpisodes.size} episods, total: ${currentList.size + newEpisodes.size}")
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

    fun loadEpisodes(urls: List<String>) {
        if (isLoadingEpisode) return
        isLoadingEpisode = true
        _stateEpisode.value = UiStateEpisode.Loading

        viewModelScope.launch {
            val episodes = mutableListOf<Episode>()
            for ((index, url) in urls.withIndex()) {
                when (val result = getEpisodeByUrlUseCase(url)) {
                    is NetworkResult.Success -> episodes.add(result.data)
                    is NetworkResult.Error -> {}
                }
                if ((index + 1) % 10 == 0) {
                    delay(5000)
                }
            }
            _stateEpisode.value = UiStateEpisode.Success(episodes)
            isLoadingEpisode = false
        }
    }

    sealed class UiStateCharacter {
        object Loading: UiStateCharacter()
        data class Success(val characters: List<Character>) : UiStateCharacter()
        data class Error(val message: String) : UiStateCharacter()
    }

    sealed class UiStateEpisode {
        object Loading: UiStateEpisode()
        data class Success(val episodes: List<Episode>): UiStateEpisode()
        data class Error(val message: String): UiStateEpisode()
    }
}