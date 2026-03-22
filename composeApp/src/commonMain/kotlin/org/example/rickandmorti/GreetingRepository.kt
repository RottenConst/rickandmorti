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

    private var currentPage = 1
    private var isLoading = false
    private var hasMorePages = true


    /**
     * Загружает следующую страницу персонажей и добавляет их к существующему списку.
     */
    suspend fun loadNextPage() {
        if (isLoading || !hasMorePages) return
        isLoading = true
        try {
            val newChars = api.getAllCharacters(currentPage)
            if (newChars.isNotEmpty()) {
                _characters.value += newChars // 🔥 Обновляем Flow
                currentPage++
                Logger.log("Repository: loaded ${newChars.size} characters, total now: ${_characters.value.size}")
            } else {
                hasMorePages = false
            }
        } catch (e: Exception) {
            Logger.log("Error in loadNextPage: ${e.message}")
            hasMorePages = false
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    suspend fun loadCharacters(): List<Character>{
        try {
            val result = api.getAllCharacters()
            return result
        } catch (e: Exception) {
            println("Error fetching characters: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Инициализирует первую загрузку данных.
     */
    suspend fun refresh() {
        currentPage = 1
        hasMorePages = true
        _characters.value = emptyList()
        loadNextPage()
    }
}