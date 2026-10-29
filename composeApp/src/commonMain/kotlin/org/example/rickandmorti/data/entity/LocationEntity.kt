package org.example.rickandmorti.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import org.example.rickandmorti.domain.model.Location

@Entity(tableName = "locations")
@Serializable
data class LocationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: String,
    val url: String,
    val created: String
) {
    fun toDomain(): Location {
        return Location(
            id = id,
            name = name,
            type = type,
            dimension = dimension,
            residents = residents.split(","),
            url = url,
            created = created
        )
    }

    companion object {
        fun fromDomain(location: Location): LocationEntity {
            return LocationEntity(
                id = location.id,
                name = location.name,
                type = location.type,
                dimension = location.dimension,
                residents = location.residents.joinToString(","),
                url = location.url,
                created = location.created
            )
        }
    }
}
