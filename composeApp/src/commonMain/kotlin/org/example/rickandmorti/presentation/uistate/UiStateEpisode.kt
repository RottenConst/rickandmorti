package org.example.rickandmorti.presentation.uistate

import org.example.rickandmorti.domain.model.Episode


sealed class UiStateEpisode {
    object Loading: UiStateEpisode()
    data class Success(val episodes: List<Episode>): UiStateEpisode()
    data class Error(val message: String): UiStateEpisode()
}