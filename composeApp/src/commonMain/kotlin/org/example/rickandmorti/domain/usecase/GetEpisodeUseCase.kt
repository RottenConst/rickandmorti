package org.example.rickandmorti.domain.usecase

import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.repository.EpisodeRepository
import org.example.rickandmorti.util.NetworkResult

class GetEpisodeUseCase(
    private val episodeRepository: EpisodeRepository
) {
    suspend operator fun invoke(page: Int?): NetworkResult<List<Episode>> {
        return episodeRepository.getEpisodes(page)
    }
}