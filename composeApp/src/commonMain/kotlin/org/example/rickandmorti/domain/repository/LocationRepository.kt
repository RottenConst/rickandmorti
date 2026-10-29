package org.example.rickandmorti.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.util.NetworkResult

interface LocationRepository {
    suspend fun getLocations(name: String?, page: Int?): NetworkResult<List<Location>>
    suspend fun getLocation(id: Int): NetworkResult<Location>
    fun getLocationsFlow(name: String? = null): Flow<List<Location>>
    fun getLocationFlow(id: Int): Flow<Location?>
    suspend fun cacheLocations(locations: List<Location>)
    suspend fun cacheLocation(location: Location)
    suspend fun clearCache()
}