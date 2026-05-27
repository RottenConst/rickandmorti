package org.example.rickandmorti.domain.usecase

import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.util.NetworkResult

class GetCharactersUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(name: String? = null, page: Int? = null): NetworkResult<List<Character>> {
        return repository.getCharacters(name = name, page = page)
    }
}