package org.example.rickandmorti

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform