package org.example.rickandmorti.data.repository

import androidx.room3.withWriteTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.example.rickandmorti.data.database.AppDatabase
import org.example.rickandmorti.data.entity.EpisodeEntity
import org.example.rickandmorti.data.mapper.toDomain
import org.example.rickandmorti.data.source.EpisodeDataSource
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.repository.EpisodeRepository
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult

class EpisodeRepositoryImpl(
    private val dataSource: EpisodeDataSource,
    private val database: AppDatabase,
    private val ioScope: CoroutineScope
): EpisodeRepository {

    override suspend fun getEpisodes(name: String?, page: Int?): NetworkResult<List<Episode>> {
        return try {
            when (val networkResult = dataSource.getEpisodes(name, page)) {
                is NetworkResult.Success -> {
                    val episodes = networkResult.data.map { it.toDomain() }.toList()
                    cacheEpisodesAsync(episodes)
                    NetworkResult.Success(episodes)
                }
                is NetworkResult.Error -> {
                    val cachedEpisode = getEpisodesFromCache(name)
                    if (cachedEpisode.isNotEmpty()) {
                        Logger.log("Returning ${cachedEpisode.size} cached episodes")
                        NetworkResult.Success(cachedEpisode)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        return networkResult as NetworkResult<List<Episode>>
                    }
                }
            }
        } catch (e: Exception) {
            Logger.log("Error in getEpisodes: ${e.message}")
            NetworkResult.Error(e)
        }
    }

    override suspend fun getEpisode(id: Int): NetworkResult<Episode> {
        return try {
            when (val networkResult = dataSource.getEpisode(id)) {
                is NetworkResult.Success -> {
                    val episode = networkResult.data.toDomain()
                    cacheEpisodeAsync(episode)
                    NetworkResult.Success(episode)
                }
                is NetworkResult.Error -> {
                    val cached = database.episodeDao().getEpisodeById(id)?.toDomain()
                    if (cached != null) {
                        Logger.log("Returning cached episode $id")
                        NetworkResult.Success(cached)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        return networkResult as NetworkResult<Episode>
                    }
                }
            }
        } catch (e: Exception) {
            val cached = getEpisodeFromCache(id)
            if (cached != null) {
                NetworkResult.Success(cached)
            } else {
                NetworkResult.Error(e)
            }
        }
    }

    override suspend fun cacheEpisodes(episodes: List<Episode>) {
        val entities = episodes.map { EpisodeEntity.fromDomain(it) }
        database.withWriteTransaction {
            database.episodeDao().insertEpisodes(entities)
        }
        Logger.log("Cached ${entities.size} episodes in Room")
    }

    override suspend fun cacheEpisode(episode: Episode) {
        database.withWriteTransaction {
            database.episodeDao().insertEpisode(EpisodeEntity.fromDomain(episode))
        }
        Logger.log("Cached episode ${episode.id} in Room")
    }

    override fun getEpisodesFlow(name: String?): Flow<List<Episode>> {
        return getEpisodesFlowFromDb(name)
    }

    override fun getEpisodeFlow(id: Int): Flow<Episode?> {
        return database.episodeDao().getEpisodeFlow(id)
            .map { entity ->
                entity?.toDomain()
            }.catch { e ->
                Logger.log("Error reading episode $id from cache ${e.message}")
                emit(null)
            }
    }

    private suspend fun getEpisodesFromCache(name: String?): List<Episode> {
        return try {
            val entities = if (name != null) {
                database.episodeDao().searchEpisodes(name)
            } else {
                database.episodeDao().getAllEpisodes()
            }.first()
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            Logger.log("Error reading from cache ${e.message}")
            emptyList()
        }
    }

    private suspend fun getEpisodeFromCache(id: Int): Episode? {
        return try {
            database.episodeDao().getEpisodeById(id)?.toDomain()
        } catch (e: Exception) {
            Logger.log("Error reading episode $id from cache: ${e.message}")
            null
        }
    }

    private fun cacheEpisodesAsync(episodes: List<Episode>) {
        ioScope.launch {
            try {
                cacheEpisodes(episodes)
            } catch (e: Exception) {
                Logger.log("Error caching episodes ${e.message}")
            }
        }
    }

    private fun cacheEpisodeAsync(episode: Episode) {
        ioScope.launch {
            try {
                cacheEpisode(episode)
            } catch (e: Exception) {
                Logger.log("Error caching episode ${e.message}")
            }
        }
    }

    private fun getEpisodesFlowFromDb(name: String?): Flow<List<Episode>> {
        return if (name != null) {
            database.episodeDao().searchEpisodes(name)
        } else {
            database.episodeDao().getAllEpisodes()
        }.map { entities ->
            entities.map { it.toDomain() }
        }.catch { e ->
            Logger.log("Error reading from cache: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun clearCache() {
        database.episodeDao().deleteAll()
        Logger.log("Clear cache")
    }

}

// Помогаем NetworkResult преобразовать тип
//fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
//    is NetworkResult.Success -> NetworkResult.Success(transform(data))
//    is NetworkResult.Error -> NetworkResult.Error(exception)
//}