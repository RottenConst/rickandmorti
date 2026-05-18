package org.example.rickandmorti.data.mapper

import org.example.rickandmorti.data.dto.EpisodeDto
import org.example.rickandmorti.domain.model.Episode


fun EpisodeDto.toDomain(): Episode = Episode(
    id = id,
    name = name,
    air_date = air_date,
    episode = episode,
    characters = characters,
    url = url,
    created = created
)