package org.example.rickandmorti.presentation.uistate

import org.example.rickandmorti.domain.model.Location

sealed class UiStateLocation {
    object Loading : UiStateLocation()
    data class Success(val locations: List<Location>, val hasMorePages: Boolean): UiStateLocation()
    data class Error(val message: String, val hasMorePages: Boolean = false): UiStateLocation()
}