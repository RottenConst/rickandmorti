package org.example.rickandmorti.data.repository

import androidx.room3.withWriteTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.example.rickandmorti.data.database.AppDatabase
import org.example.rickandmorti.data.entity.LocationEntity
import org.example.rickandmorti.data.mapper.toDomain
import org.example.rickandmorti.data.source.LocationDataSource
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.domain.repository.LocationRepository
import org.example.rickandmorti.util.Logger
import org.example.rickandmorti.util.NetworkResult

class LocationRepositoryImpl(
    private val dataSource: LocationDataSource,
    private val database: AppDatabase,
    private val ioScope: CoroutineScope
): LocationRepository {

    override suspend fun getLocations(
        name: String?,
        page: Int?
    ): NetworkResult<List<Location>> {
        return try {
            when (val networkResult = dataSource.getAllLocations(name, page)) {
                is NetworkResult.Success -> {
                    val locations = networkResult.data.map { it.toDomain() }.toList()
                    cacheLocationsAsync(locations)
                    NetworkResult.Success(locations)
                }
                is NetworkResult.Error -> {
                    val cachedLocations = getLocationsFromCache(name)
                    if (cachedLocations.isNotEmpty()) {
                        Logger.log("Returning ${cachedLocations.size} cached locations")
                        NetworkResult.Success(cachedLocations)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        return networkResult as NetworkResult<List<Location>>
                    }
                }
            }
        } catch (e: Exception) {
            Logger.log("Error in getLocations: ${e.message}")
            NetworkResult.Error(e)
        }
    }

    override suspend fun getLocation(id: Int): NetworkResult<Location> {
        return try {
            when (val networkResult = dataSource.getLocation(id)) {
                is NetworkResult.Success -> {
                    val location = networkResult.data.toDomain()
                    cacheLocationAsync(location)
                    NetworkResult.Success(location)
                }
                is NetworkResult.Error -> {
                    val cachedLocation = database.locationDao().getLocationById(id)?.toDomain()
                    if (cachedLocation != null) {
                        Logger.log("Returning cached location $id")
                        NetworkResult.Success(cachedLocation)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        return networkResult as NetworkResult<Location>
                    }
                }
            }
        } catch (e: Exception) {
            val cachedLocation = getLocationFromCache(id)
            if (cachedLocation != null) {
                NetworkResult.Success(cachedLocation)
            } else {
                NetworkResult.Error(e)
            }
        }
    }

    override suspend fun cacheLocations(locations: List<Location>) {
        val entities = locations.map { LocationEntity.fromDomain(it) }
        database.withWriteTransaction {
            database.locationDao().insertLocations(entities)
        }
        Logger.log("Cached ${entities.size} locations in Room")
    }

    override suspend fun cacheLocation(location: Location) {
        database.withWriteTransaction {
            database.locationDao().insertLocation(LocationEntity.fromDomain(location))
        }
        Logger.log("Cached location ${location.id} in Room")
    }

    override fun getLocationsFlow(name: String?): Flow<List<Location>> {
        return getLocationsFlowFromDb(name)
    }

    override fun getLocationFlow(id: Int): Flow<Location?> {
        return database.locationDao().getLocationFlow(id)
            .map { entity ->
                entity?.toDomain()
            }.catch { e ->
                Logger.log("Error reading location $id from cache ${e.message}")
                emit(null)
            }
    }

    private suspend fun getLocationsFromCache(name: String?): List<Location> {
        return try {
            val entities = if (name != null) {
                database.locationDao().searchLocations(name)
            } else {
                database.locationDao().getAllLocations()
            }.first()
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            Logger.log("Error reading from cache ${e.message}")
            emptyList()
        }
    }

    private suspend fun getLocationFromCache(id: Int): Location? {
        return try {
            database.locationDao().getLocationById(id)?.toDomain()
        } catch (e: Exception) {
            Logger.log("Error reading location $id from cache: ${e.message}")
            null
        }
    }

    private fun cacheLocationsAsync(locations: List<Location>) {
        ioScope.launch {
            try {
                cacheLocations(locations)
            } catch (e: Exception) {
                Logger.log("Error caching locations ${e.message}")
            }
        }
    }

    private fun cacheLocationAsync(location: Location) {
        ioScope.launch {
            try {
                cacheLocation(location)
            } catch (e: Exception) {
                Logger.log("Error caching location ${e.message}")
            }
        }
    }

    private fun getLocationsFlowFromDb(name: String?): Flow<List<Location>> {
        return if (name != null) {
            database.locationDao().searchLocations(name)
        } else {
            database.locationDao().getAllLocations()
        }.map { entities ->
            entities.map { it.toDomain() }
        }.catch { e ->
            Logger.log("Error reading from cache: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun clearCache() {

    }

}