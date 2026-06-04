package org.example.rickandmorti.data.mapper

import org.example.rickandmorti.data.dto.CharacterDto
import org.example.rickandmorti.data.dto.LocationInfoDto
import org.example.rickandmorti.data.dto.OriginDto
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.LocationInfo
import org.example.rickandmorti.domain.model.Origin


fun CharacterDto.toDomain(): Character =
    Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = origin.toDomain(),
        location = location.toDomain(),
        image = image,
        episode = episode,
        url = url,
        created = created
    )

private fun OriginDto.toDomain() =
    Origin(name, url)

private fun LocationInfoDto.toDomain() =
    LocationInfo(name, url)