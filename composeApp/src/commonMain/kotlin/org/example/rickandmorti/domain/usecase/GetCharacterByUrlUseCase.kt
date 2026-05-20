package org.example.rickandmorti.domain.usecase

import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult

class GetCharacterByUrlUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(url: String): NetworkResult<Character> {
        val id = url.split("/").last().toIntOrNull() ?: return NetworkResult.Error(
            IllegalArgumentException("Invalid URL: $url")
        )
        Logger.log("GetCharacterByUrlUseCase: id $id")
        return invokeById(id)
    }

    private suspend fun invokeById(id: Int): NetworkResult<Character> {
        return try {
            when (val characterDto = repository.getCharacter(id)) {
                is NetworkResult.Success -> {
                    Logger.log(" GetCharacterByUrlUseCase: id ${characterDto.data}")
                    NetworkResult.Success(characterDto.data)
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(characterDto.exception)
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}