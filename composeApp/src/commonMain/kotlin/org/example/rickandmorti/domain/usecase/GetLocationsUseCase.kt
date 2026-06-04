package org.example.rickandmorti.domain.usecase

import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.domain.repository.LocationRepository
import org.example.rickandmorti.util.NetworkResult

class GetLocationsUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(name: String? = null, page: Int? = null): NetworkResult<List<Location>> {
        return repository.getLocations(name = name, page = page)
    }
}