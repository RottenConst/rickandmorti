package org.example.rickandmorti.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import org.example.rickandmorti.domain.model.Episode

@Entity(tableName = "episodes")
@Serializable
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val air_date: String,
    val episode: String,
    val characters: String,
    val url: String,
    val created: String
) {
    fun toDomain(): Episode {
        return Episode(
            id = id,
            name = name,
            air_date = air_date,
            episode = episode,
            characters = characters.split(","),
            url = url,
            created = created
        )
    }

    companion object {
        fun fromDomain(episode: Episode): EpisodeEntity {
            return EpisodeEntity(
                id = episode.id,
                name = episode.name,
                air_date = episode.air_date,
                episode = episode.episode,
                characters = episode.characters.joinToString(","),
                url = episode.url,
                created = episode.created
            )
        }
    }
}
