package org.example.rickandmorti.data.repository

import org.example.rickandmorti.data.mapper.toDomain
import org.example.rickandmorti.data.source.LocationDataSource
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.domain.repository.LocationRepository
import org.example.rickandmorti.util.NetworkResult

class LocationRepositoryImpl(
    private val dataSource: LocationDataSource
): LocationRepository {
    override suspend fun getLocations(
        name: String?,
        page: Int?
    ): NetworkResult<List<Location>> {
        return dataSource.getAllLocations(name, page).map { locationDto ->
            locationDto.map { it.toDomain() }
        }
    }

    override suspend fun getLocation(id: Int): NetworkResult<Location> {
        return dataSource.getLocation(id).map { it.toDomain() }
    }

}