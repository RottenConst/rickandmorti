package org.example.rickandmorti.data.repository

import org.example.rickandmorti.data.mapper.toDomain
import org.example.rickandmorti.data.source.CharacterDataSource
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.util.NetworkResult

class CharacterRepositoryImpl(
    private val dataSource: CharacterDataSource
): CharacterRepository {
    override suspend fun getCharacters(page: Int?): NetworkResult<List<Character>> {
        return dataSource.getCharacters(page).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }
}

// Помогаем NetworkResult преобразовать тип
private fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error -> NetworkResult.Error(exception)
}