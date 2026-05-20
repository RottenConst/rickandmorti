package org.example.rickandmorti.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.example.rickandmorti.data.Response
import org.example.rickandmorti.data.dto.CharacterDto
import org.example.rickandmorti.data.dto.EpisodeDto

class RickAndMortyApi(private val client: HttpClient) {
    suspend fun getAllCharacters(page: Int? = null): List<CharacterDto> {
        return client.get("/api/character") {
            if (page != null) {
                parameter("page", page)
            }
        }.body<Response<CharacterDto>>().results
    }

    suspend fun getAllEpisodes(page: Int? = null): List<EpisodeDto> {
        return client.get("/api/episode") {
            if (page != null) {
                parameter("page", page)
            }
        }.body<Response<EpisodeDto>>().results
    }

    suspend fun getCharacter(id: Int): CharacterDto {
        return client.get("/api/character/$id").body()
    }

    suspend fun getEpisode(id: Int): EpisodeDto {
        return client.get("/api/episode/$id").body()
    }
}