package org.example.rickandmorti.data.source

import org.example.rickandmorti.data.api.RickAndMortyApi
import org.example.rickandmorti.data.dto.EpisodeDto
import org.example.rickandmorti.util.NetworkResult

class EpisodeDataSource (
    private val api: RickAndMortyApi
) {
    suspend fun getEpisodes(page: Int? = null): NetworkResult<List<EpisodeDto>> {
        return try {
            val response = if (page != null) {
                api.getAllEpisodes(page)
            } else {
                api.getAllEpisodes()
            }
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

    suspend fun getEpisode(id: Int): NetworkResult<EpisodeDto> {
        return try {
            val episodeDto = api.getEpisode(id)
            NetworkResult.Success(episodeDto)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}