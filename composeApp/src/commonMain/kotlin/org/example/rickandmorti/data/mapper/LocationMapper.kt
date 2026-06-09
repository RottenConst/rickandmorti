package org.example.rickandmorti.data.mapper

import org.example.rickandmorti.data.dto.LocationDto
import org.example.rickandmorti.domain.model.Location

fun LocationDto.toDomain(): Location = Location(
    id = id,
    name = name,
    type = type,
    dimension = dimension,
    residents = residents,
    url = url,
    created = created
)