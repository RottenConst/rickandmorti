package org.example.rickandmorti.domain.usecase

import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.repository.EpisodeRepository
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult

class GetEpisodeByUrlUseCase(
    private val repository: EpisodeRepository
) {
    suspend operator fun invoke(url: String): NetworkResult<Episode> {
        Logger.log("🔍 GetEpisodeByUrlUseCase: loading $url")
        val id = url.split("/").last().toIntOrNull() ?: return NetworkResult.Error(IllegalArgumentException("Invalid URL: $url"))
        Logger.log("🔍 GetEpisodeByUrlUseCase: id $id")
        return invokeById(id)
    }

    private suspend fun invokeById(id: Int): NetworkResult<Episode> {
        return try {
            val episodeDto = repository.getEpisode(id)
            when (episodeDto) {
                is NetworkResult.Success -> {
                    Logger.log("🔍 GetEpisodeByUrlUseCase: id ${episodeDto.data}")
                    NetworkResult.Success(episodeDto.data)
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(episodeDto.exception)
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}