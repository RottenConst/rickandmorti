package org.example.rickandmorti.data.repository

import org.example.rickandmorti.data.mapper.toDomain
import org.example.rickandmorti.data.source.EpisodeDataSource
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.repository.EpisodeRepository
import org.example.rickandmorti.util.NetworkResult

class EpisodeRepositoryImpl(
    private val dataSource: EpisodeDataSource
): EpisodeRepository {
    override suspend fun getEpisodes(name: String?, page: Int?): NetworkResult<List<Episode>> {
        return dataSource.getEpisodes(name, page).map { dtoEpisode ->
            dtoEpisode.map { it.toDomain() }
        }
    }

    override suspend fun getEpisode(id: Int): NetworkResult<Episode> {
        return dataSource.getEpisode(id).map { it.toDomain() }
    }

}

// Помогаем NetworkResult преобразовать тип
fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error -> NetworkResult.Error(exception)
}