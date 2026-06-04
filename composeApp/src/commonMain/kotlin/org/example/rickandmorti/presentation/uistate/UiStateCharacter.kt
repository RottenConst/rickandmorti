package org.example.rickandmorti.presentation.uistate

import org.example.rickandmorti.domain.model.Character

sealed class UiStateCharacter {
    object Loading : UiStateCharacter()
    data class Success(val characters: List<Character>) : UiStateCharacter()
    data class Error(val message: String) : UiStateCharacter()
}