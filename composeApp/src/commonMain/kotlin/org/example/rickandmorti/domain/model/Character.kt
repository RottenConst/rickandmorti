package org.example.rickandmorti.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: Origin,
    val location: LocationInfo,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)


@Serializable
data class Origin(val name: String, val url: String)

@Serializable
data class LocationInfo(val name: String, val url: String)