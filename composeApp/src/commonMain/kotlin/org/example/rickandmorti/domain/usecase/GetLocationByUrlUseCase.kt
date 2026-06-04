package org.example.rickandmorti.domain.usecase

import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.domain.repository.LocationRepository
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult

class GetLocationByUrlUseCase(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(url: String): NetworkResult<Location> {
        val id = url.split("/").last().toIntOrNull() ?: return NetworkResult.Error(
            IllegalArgumentException("Invalid URL $url")
        )
        Logger.log("GetLocationByUrlUseCase invoked with ID=$id")
        return invokeById(id)
    }

    private suspend fun invokeById(id: Int): NetworkResult<Location> {
        return try {
            when(val locationDto = repository.getLocation(id)) {
                is NetworkResult.Success -> {
                    Logger.log("GetLocationByUrlUseCase: id ${locationDto.data}")
                    NetworkResult.Success(locationDto.data)
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(locationDto.exception)
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}