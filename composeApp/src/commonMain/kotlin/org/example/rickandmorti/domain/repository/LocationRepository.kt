package org.example.rickandmorti.domain.repository

import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.util.NetworkResult

interface LocationRepository {
    suspend fun getLocations(name: String?, page: Int?): NetworkResult<List<Location>>
    suspend fun getLocation(id: Int): NetworkResult<Location>
}