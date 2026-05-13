package org.example.rickandmorti.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult
import org.koin.core.component.KoinComponent

class CharacterViewModel(
    private val getCharactersUseCase: GetCharactersUseCase
) : BaseViewModel(), KoinComponent {
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    private var currentPage = 1
    private var isLoading = false
    private var hasMorePages = true

    init {
        loadNextPage()
    }

    fun loadNextPage(){
        if (isLoading || !hasMorePages) return
        isLoading = true

        viewModelScope.launch {
            delay(300)

            when (val result = getCharactersUseCase(currentPage)) {
                is NetworkResult.Success -> {
                    val newChars = result.data
                    if (newChars.isNotEmpty()) {
                        val currentList = (_state.value as? UiState.Success)?.characters.orEmpty()
                        _state.value = UiState.Success(currentList + newChars)
                        currentPage++
                        Logger.log(message = "VM: loaded ${newChars.size} chars, total: ${currentList.size + newChars.size}")
                    } else {
                        hasMorePages = false
                    }
                }
                is NetworkResult.Error -> {
                    _state.value = UiState.Error(result.exception.message ?: "Unknown error")
                    hasMorePages = false
                    Logger.log("VM Error: ${result.exception}")
                }
            }
            isLoading = false
        }
    }

    fun refresh() {
        currentPage = 1
        hasMorePages = true
        loadNextPage()
    }

    sealed class UiState {
        object Loading: UiState()
        data class Success(val characters: List<Character>) : UiState()
        data class Error(val message: String) : UiState()
    }
}