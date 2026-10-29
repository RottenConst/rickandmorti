package org.example.rickandmorti.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.util.NetworkResult

interface EpisodeRepository {
    suspend fun getEpisodes(name: String?, page: Int?): NetworkResult<List<Episode>>
    suspend fun getEpisode(id: Int): NetworkResult<Episode>
    fun getEpisodesFlow(name: String? = null): Flow<List<Episode>>
    fun getEpisodeFlow(id: Int): Flow<Episode?>
    suspend fun cacheEpisodes(episodes: List<Episode>)
    suspend fun cacheEpisode(episode: Episode)
    suspend fun clearCache()
}