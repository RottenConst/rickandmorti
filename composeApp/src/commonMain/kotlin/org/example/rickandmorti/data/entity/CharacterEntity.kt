package org.example.rickandmorti.data.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.LocationInfo
import org.example.rickandmorti.domain.model.Origin

@Entity(tableName = "characters")
@Serializable
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    @ColumnInfo(name = "origin_name")
    val originName: String,
    @ColumnInfo(name = "origin_url")
    val originUrl: String,
    @ColumnInfo(name = "location_name")
    val locationName: String,
    @ColumnInfo(name = "location_url")
    val locationUrl: String,
    val image: String,
    val episode: String,
    val url: String,
    val created: String
) {
    fun toDomain(): Character {
        return Character(
            id = id,
            name = name,
            status = status,
            species = species,
            type = type,
            gender = gender,
            origin = Origin(originName, originUrl),
            location = LocationInfo(locationName, locationUrl),
            image = image,
            episode = episode.split(","),
            url = url,
            created = created
        )
    }

    companion object {
        fun fromDomain(character: Character): CharacterEntity {
            return CharacterEntity(
                id = character.id,
                name = character.name,
                status = character.status,
                species = character.species,
                type = character.type,
                gender = character.gender,
                originName = character.origin.name,
                originUrl = character.origin.url,
                locationName = character.location.name,
                locationUrl = character.location.url,
                image = character.image,
                episode = character.episode.joinToString(","),
                url = character.url,
                created = character.created
            )
        }
    }
}
