package org.example.rickandmorti.presentation.uistate

import org.example.rickandmorti.domain.model.Character

sealed class UiStateCharacter {
    object Loading : UiStateCharacter()
    data class Success(val characters: List<Character>, val hasMorePages: Boolean) : UiStateCharacter()
    data class Error(val message: String, val hasMorePages: Boolean) : UiStateCharacter()
}