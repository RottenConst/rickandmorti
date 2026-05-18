package org.example.rickandmorti.data

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val info: Info,
    val results: List<T>
)

@Serializable
data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)