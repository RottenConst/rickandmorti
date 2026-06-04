package org.example.rickandmorti.domain.repository

import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.util.NetworkResult

interface EpisodeRepository {
    suspend fun getEpisodes(name: String?, page: Int?): NetworkResult<List<Episode>>
    suspend fun getEpisode(id: Int): NetworkResult<Episode>
}