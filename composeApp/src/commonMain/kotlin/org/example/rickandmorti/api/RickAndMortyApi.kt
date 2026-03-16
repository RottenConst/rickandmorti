package org.example.rickandmorti.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.example.rickandmorti.data.Character
import org.example.rickandmorti.data.CharacterResponse

class RickAndMortyApi(private val client: HttpClient) {
    suspend fun getAllCharacters(): List<Character> {
        return client.get("https://rickandmortyapi.com/api/character")
            .body<CharacterResponse>()
            .results
    }
}