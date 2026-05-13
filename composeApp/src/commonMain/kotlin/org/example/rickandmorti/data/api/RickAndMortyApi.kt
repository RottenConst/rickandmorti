package org.example.rickandmorti.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.example.rickandmorti.data.CharacterResponse
import org.example.rickandmorti.data.dto.CharacterDto

class RickAndMortyApi(private val client: HttpClient) {
    suspend fun getAllCharacters(page: Int? = null): List<CharacterDto> {
        return client.get("/api/character") {
            if (page != null) {
                parameter("page", page)
            }
        }.body<CharacterResponse>().results
    }
}