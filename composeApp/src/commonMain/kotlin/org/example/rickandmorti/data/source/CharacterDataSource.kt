package org.example.rickandmorti.data.source

import org.example.rickandmorti.data.api.RickAndMortyApi
import org.example.rickandmorti.data.dto.CharacterDto
import org.example.rickandmorti.util.NetworkResult

class CharacterDataSource(
    private val api: RickAndMortyApi
) {
    suspend fun getCharacters(page: Int? = null): NetworkResult<List<CharacterDto>> {
        return try {
            val response = if (page != null) {
                api.getAllCharacters(page)
            } else {
                api.getAllCharacters()
            }
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}