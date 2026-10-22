package org.example.rickandmorti.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.util.NetworkResult

interface CharacterRepository {
    suspend fun getCharacters(name: String?, page: Int?): NetworkResult<List<Character>>
    suspend fun getCharacter(id: Int): NetworkResult<Character>
    fun getCharactersFlow(name: String? = null): Flow<List<Character>>
    fun getCharacterFlow(id: Int): Flow<Character?>
    suspend fun cacheCharacters(characters: List<Character>)
    suspend fun cacheCharacter(character: Character)
    suspend fun clearCache()
}