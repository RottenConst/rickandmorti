package org.example.rickandmorti.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents : List<String>,
    val url: String,
    val created:String
)
