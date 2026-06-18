package org.example.rickandmorti.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.example.rickandmorti.data.Response
import org.example.rickandmorti.data.dto.CharacterDto
import org.example.rickandmorti.data.dto.EpisodeDto
import org.example.rickandmorti.data.dto.LocationDto

class RickAndMortyApi(private val client: HttpClient) {
    suspend fun getAllCharacters(name: String? = null, page: Int? = null): List<CharacterDto> {
        return try {
            val response = client.get("/api/character") {
                if (page != null) {
                    parameter("page", page)
                }
                if (name != null) {
                    parameter("name", name)
                }
            }
            response.body<Response<CharacterDto>>().results
        } catch (e: Exception) {
            throw Exception("Failed to fetch characters (name=$name, page=$page): ${e.message}", e)
        }
    }

    suspend fun getAllEpisodes(
        name: String? = null,
        page: Int? = null
    ): List<EpisodeDto> {
        return try {
            val response = client.get("/api/episode") {
                if (page != null) {
                    parameter("page", page)
                }
                if (name != null) {
                    parameter("name", name)
                }
            }
            val body = response.body<Response<EpisodeDto>>()
            body.results
        } catch (e: Exception) {
            throw Exception("Failed to fetch episodes (name=$name, page=$page): ${e.message}", e)
        }
    }

    suspend fun getAllLocations(
        name: String? = null,
        page: Int? = null
    ): List<LocationDto> {
        return try {
            val response = client.get("/api/location") {
                if (page != null) {
                    parameter("page", page)
                }
                if (name != null) {
                    parameter("name", name)
                }
            }
            response.body<Response<LocationDto>>().results
        } catch (e: Exception) {
            throw Exception("Failed to fetch locations (name=$name, page=$page): ${e.message}", e)
        }
    }

    suspend fun getCharacter(id: Int): CharacterDto {
        return client.get("/api/character/$id").body()
    }

    suspend fun getEpisode(id: Int): EpisodeDto {
        return client.get("/api/episode/$id").body()
    }

    suspend fun getLocation(id: Int): LocationDto {
        return client.get("/api/location/$id").body()
    }
}