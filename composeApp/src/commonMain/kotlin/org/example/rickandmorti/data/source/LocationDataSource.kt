package org.example.rickandmorti.data.source

import org.example.rickandmorti.data.api.RickAndMortyApi
import org.example.rickandmorti.data.dto.LocationDto
import org.example.rickandmorti.util.NetworkResult

class LocationDataSource(
    private val api: RickAndMortyApi
) {
    suspend fun getAllLocations(
        name: String? = null,
        page: Int? = null
    ): NetworkResult<List<LocationDto>> {
        return try {
            val response = if (page != null) {
                if (name != null) {
                    api.getAllLocations(name = name, page = page)
                } else {
                    api.getAllLocations(page = page)
                }
            } else {
                api.getAllLocations()
            }
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

    suspend fun getLocation(id: Int): NetworkResult<LocationDto> {
        return try {
            val locationDto = api.getLocation(id)
            NetworkResult.Success(locationDto)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}