package org.example.rickandmorti

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import org.example.rickandmorti.api.RickAndMortyApi
import org.example.rickandmorti.data.Character

class GreetingRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            url ("https://rickandmortyapi.com/api")
        }
    }

    private val api = RickAndMortyApi(client)

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters

    suspend fun loadCharacters(){
        try {
            val result = api.getAllCharacters()
            _characters.value = result
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun formatGreetings(name: String): String{
        return "Hello from $name! Welcome to Compose Multiplatform!"
    }
}