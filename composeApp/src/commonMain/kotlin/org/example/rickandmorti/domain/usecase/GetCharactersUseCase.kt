package org.example.rickandmorti.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.util.NetworkResult

class GetCharactersUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(name: String? = null, page: Int? = null): NetworkResult<List<Character>> {
        return repository.getCharacters(name = name, page = page)
    }

    suspend operator fun invoke(id: Int): NetworkResult<Character?> {
        return repository.getCharacter(id)
    }

    fun fromCache(name: String? = null): Flow<List<Character>> {
        return repository.getCharactersFlow(name)
    }
}