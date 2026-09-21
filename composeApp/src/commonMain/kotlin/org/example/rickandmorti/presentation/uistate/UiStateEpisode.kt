package org.example.rickandmorti.presentation.uistate

import org.example.rickandmorti.domain.model.Episode


sealed class UiStateEpisode {
    object Loading: UiStateEpisode()
    data class Success(val episodes: List<Episode>, val hasMorePages: Boolean): UiStateEpisode()
    data class Error(val message: String, val hasMorePages: Boolean = false): UiStateEpisode()
}