package org.example.rickandmorti.data.repository

import androidx.room3.withWriteTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.example.rickandmorti.data.database.AppDatabase
import org.example.rickandmorti.data.entity.CharacterEntity
import org.example.rickandmorti.data.mapper.toDomain
import org.example.rickandmorti.data.source.CharacterDataSource
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult

class CharacterRepositoryImpl(
    private val dataSource: CharacterDataSource,
    private val database: AppDatabase,
    private val ioScope: CoroutineScope
): CharacterRepository {

    override suspend fun getCharacters(name: String?, page: Int?): NetworkResult<List<Character>> {
        return try {
            when (val networkResult = dataSource.getCharacters(name, page)) {
                is NetworkResult.Success -> {
                    // Конвертируем DTO в Domain модель
                    val characters = networkResult.data.map { it.toDomain() }.toList()
                    cacheCharactersAsync(characters)
                    // Кэшируем данные в Room
                    Logger.log("Cached ${characters.size} characters in Room 3")
                    NetworkResult.Success(characters)
                }
                is NetworkResult.Error -> {
                    // Возвращаем кэшированные данные при ошибке сети
                    val cachedData = getCharactersFromCache(name, page)
                    if (cachedData.isNotEmpty()) {
                        Logger.log("Returning ${cachedData.size} cached characters")
                        NetworkResult.Success(cachedData)
                    } else {
                        // Возвращаем оригинальную ошибку сети
                        @Suppress("UNCHECKED_CAST")
                        return networkResult as NetworkResult<List<Character>>
                    }
                }
            }
        } catch (e: Exception) {
            Logger.log("Error in getCharacters: ${e.message}")
            NetworkResult.Error(e)
        }
    }

    override suspend fun getCharacter(id: Int): NetworkResult<Character> {
        return try {
            when (val networkResult = dataSource.getCharacter(id)) {
                is NetworkResult.Success -> {
                    // Конвертируем DTO в Domain модель
                    val character = networkResult.data.toDomain()
                    cacheCharacterAsync(character)
                    NetworkResult.Success(character)
                }
                is NetworkResult.Error -> {
                    val cached = database.characterDao().getCharacterById(id)?.toDomain()
                    if (cached != null) {
                        Logger.log("Returning cached character $id")
                        NetworkResult.Success(cached)
                    } else {
                        // Возвращаем оригинальную ошибку сети
                        @Suppress("UNCHECKED_CAST")
                        return networkResult as NetworkResult<Character>
                    }
                }
            }
        } catch (e: Exception) {
            val cached = getCharacterFromCache(id)
            if (cached != null) {
                NetworkResult.Success(cached)
            } else {
                NetworkResult.Error(e)
            }
        }
    }

    override fun getCharactersFlow(name: String?): Flow<List<Character>> {
        return getCharacterFlowFromDb(name)
    }

    override fun getCharacterFlow(id: Int): Flow<Character?> {
        return database.characterDao().getCharacterFlow(id)
            .map { entity ->
                entity?.toDomain()
            }.catch { e ->
                Logger.log("Error reading character $id from cache ${e.message}")
                emit(null)
            }
    }

    override suspend fun cacheCharacters(characters: List<Character>) {
        val entities = characters.map { CharacterEntity.fromDomain(it) }
        database.withWriteTransaction {
            database.characterDao().insertCharacters(entities)
        }
        Logger.log("Cached ${entities.size} character in Room")
    }

    override suspend fun cacheCharacter(character: Character) {
        database.withWriteTransaction {
            database.characterDao().insertCharacter(CharacterEntity.fromDomain(character))
        }
    }

    private suspend fun getCharactersFromCache(name: String?, page: Int?): List<Character> {
        return try {
            val entities = if (name != null) {
                database.characterDao().searchCharacters(name)
            } else {
                database.characterDao().getAllCharacters()
            }.first()
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            Logger.log("Error reading from cache ${e.message}")
            emptyList()
        }
    }

    private suspend fun getCharacterFromCache(id: Int): Character? {
        return try {
            database.characterDao().getCharacterById(id)?.toDomain()
        } catch (e: Exception) {
            Logger.log("Error reading character $id from cache: ${e.message}")
            null
        }
    }

    private fun cacheCharactersAsync(characters: List<Character>) {
        ioScope.launch {
            try {
                cacheCharacters(characters)
            } catch (e: Exception) {
                Logger.log("Error caching characters ${e.message}")
            }
        }
    }

    private fun cacheCharacterAsync(character: Character) {
        ioScope.launch {
            try {
                cacheCharacter(character)
            } catch (e: Exception) {
                Logger.log("Error caching character ${e.message}")
            }
        }
    }

    private fun getCharacterFlowFromDb(name: String?): Flow<List<Character>> {
        return if (name != null) {
            database.characterDao().searchCharacters(name)
        } else {
            database.characterDao().getAllCharacters()
        }.map { entities ->
            entities.map { it.toDomain() }
        }.catch { e ->
            Logger.log("Error reading from cache: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun clearCache() {
        database.characterDao().deleteAll()
        Logger.log("Cache cleared")
    }
}