package org.example.rickandmorti.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeByUrlUseCase
import org.example.rickandmorti.presentation.uistate.UiStateCharacter
import org.example.rickandmorti.presentation.uistate.UiStateEpisode
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.milliseconds

class CharacterViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getEpisodeByUrlUseCase: GetEpisodeByUrlUseCase
) : BaseViewModel(), KoinComponent {
    private val _stateCharacter = MutableStateFlow<UiStateCharacter>(UiStateCharacter.Loading)
    val stateCharacter: StateFlow<UiStateCharacter> = _stateCharacter

    private val _stateEpisode = MutableStateFlow<UiStateEpisode>(UiStateEpisode.Loading)
    val stateEpisode: StateFlow<UiStateEpisode> = _stateEpisode

    private var currentPage = 1
    private var isLoading = false

    private var isLoadingEpisode = false
    private var hasMorePages = true

    init {
        loadedAllCharacters()
    }

    fun loadedAllCharacters(name: String? = null){
        val query = name.takeIf { it?.isNotBlank() == true }
        Logger.log("VM Searched for $query")
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            delay(300.milliseconds)

            when (val result = getCharactersUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val newChars = result.data
                    if (newChars.isNotEmpty()) {
                        val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                        val uniqueNewCharacter = newChars.filterNot { existingChar ->
                            currentList.any { it.id == existingChar.id }
                        }
                        val updatedCharList = currentList + uniqueNewCharacter
                        _stateCharacter.value = UiStateCharacter.Success(updatedCharList)
                        currentPage++
                        hasMorePages = newChars.isNotEmpty()
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
        if (!hasMorePages) {
            Logger.log("❌ loadNextPage: hasMorePages = false, return")
            return
        }

        val query = name.takeIf { it?.isNotBlank() == true }

        viewModelScope.launch {
            when (val result = getCharactersUseCase(name = query, page = currentPage)) {
                is NetworkResult.Success -> {
                    val currentList = (_stateCharacter.value as? UiStateCharacter.Success)?.characters.orEmpty()
                    val currentIds = currentList.map { it.id }.toSet()

                    val newChars = result.data
                    val uniqueNewCharacter = newChars.filterNot { it.id in currentIds }

                    val updatedCharsList = currentList + uniqueNewCharacter
                    _stateCharacter.value = UiStateCharacter.Success(updatedCharsList)
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

        loadedAllCharacters(query)
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
                if (index + 1 % 10 == 0) {
                    delay(5000.milliseconds)
                }
            }
            _stateEpisode.value = UiStateEpisode.Success(episodes)
            isLoadingEpisode = false
        }
    }
}