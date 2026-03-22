package org.example.rickandmorti.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.example.rickandmorti.data.Character
import org.example.rickandmorti.data.CharacterResponse

class RickAndMortyApi(private val client: HttpClient) {
    suspend fun getAllCharacters(page: Int = 1): List<Character> {
        return client.get("/api/character?page=$page")
            .body<CharacterResponse>()
            .results
    }
}