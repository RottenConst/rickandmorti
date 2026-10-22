package org.example.rickandmorti.data.repository

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.example.rickandmorti.data.dao.CharacterDao
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
    database: AppDatabase
): CharacterRepository {

    private val characterDao: CharacterDao = database.characterDao()

    override suspend fun getCharacters(name: String?, page: Int?): NetworkResult<List<Character>> {
        return try {
            val networkResult = dataSource.getCharacters(name, page)

            when (networkResult) {
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
                    val cached = characterDao.getCharacterById(id)?.toDomain()
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
            val cached = characterDao.getCharacterById(id)?.toDomain()
            if (cached != null) {
                NetworkResult.Success(cached)
            } else {
                NetworkResult.Error(e)
            }
        }
    }

    override fun getCharactersFlow(name: String?): Flow<List<Character>> {
        return if (name != null) {
            characterDao.searchCharacters(name)
        } else {
            characterDao.getAllCharacters()
        }.map { entities ->
            entities.map { it.toDomain() }
        }.catch { e ->
            Logger.log("Error reading from cache: ${e.message}")
            emit(emptyList())
        }
    }

    override fun getCharacterFlow(id: Int): Flow<Character?> {
        return characterDao.getCharacterFlow(id).map { entity ->
            entity?.toDomain()
        }.catch { e ->
            Logger.log("Error reading character $id from cache ${e.message}")
            emit(null)
        }
    }

    override suspend fun cacheCharacters(characters: List<Character>) {
        val entities = characters.map { CharacterEntity.fromDomain(it) }
        characterDao.insertCharacters(entities)
        Logger.log("Cached ${entities.size} character in Room")
    }

    override suspend fun cacheCharacter(character: Character) {
        characterDao.insertCharacter(CharacterEntity.fromDomain(character))
    }

    private suspend fun getCharactersFromCache(name: String?, page: Int?): List<Character> {
        return try {
            val allCached = characterDao.getAllCharacters().firstOrNull() ?: emptyList()
            val filtered = if (name != null) {
                allCached.filter { it.name.contains(name, ignoreCase = true) }
            } else {
                allCached
            }
            filtered.map { it.toDomain() }.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun cacheCharactersAsync(characters: List<Character>) {
        GlobalScope.launch {
            cacheCharacters(characters)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun cacheCharacterAsync(character: Character) {
        GlobalScope.launch {
            cacheCharacter(character)
        }
    }

    fun getCharactersFromCacheFlow(name: String? = null): Flow<List<Character>> {
        return if (name != null) {
            characterDao.searchCharacters(name).map { entities ->
                entities.map { it.toDomain() }.toList()
            }
        } else {
            characterDao.getAllCharacters().map { entities ->
                entities.map { it.toDomain() }.toList()
            }
        }.catch { e ->
            Logger.log("Error reading from cache: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun clearCache() {
        characterDao.deleteAll()
        Logger.log("Cache cleared")
    }
}