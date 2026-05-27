package org.example.rickandmorti.data.source

import org.example.rickandmorti.data.api.RickAndMortyApi
import org.example.rickandmorti.data.dto.CharacterDto
import org.example.rickandmorti.util.NetworkResult

class CharacterDataSource(
    private val api: RickAndMortyApi
) {
    suspend fun getCharacters(
        name: String? = null,
        page: Int? = null
    ): NetworkResult<List<CharacterDto>> {
        return try {
            val response = if (page != null) {
                if (name != null) {
                    api.getAllCharacters(name = name, page = page)
                } else {
                    api.getAllCharacters(page = page)
                }
            } else {
                api.getAllCharacters()
            }
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

    suspend fun getCharacter(id: Int): NetworkResult<CharacterDto> {
        return try {
            val characterDto = api.getCharacter(id)
            NetworkResult.Success(characterDto)
        }catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}