package org.example.rickandmorti.domain.repository

import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.util.NetworkResult

interface CharacterRepository {
    suspend fun getCharacters(page: Int?): NetworkResult<List<Character>>
    suspend fun getCharacter(id: Int): NetworkResult<Character>
}