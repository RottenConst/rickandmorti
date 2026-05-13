package org.example.rickandmorti.data

import kotlinx.serialization.Serializable
import org.example.rickandmorti.data.dto.CharacterDto

@Serializable
data class CharacterResponse(
    val info: Info,
    val results: List<CharacterDto>
)

@Serializable
data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)