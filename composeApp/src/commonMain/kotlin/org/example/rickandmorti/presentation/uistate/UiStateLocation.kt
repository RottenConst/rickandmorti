package org.example.rickandmorti.presentation.uistate

import org.example.rickandmorti.domain.model.Location

sealed class UiStateLocation {
    object Loading : UiStateLocation()
    data class Success(val locations: List<Location>): UiStateLocation()
    data class Error(val message: String): UiStateLocation()
}