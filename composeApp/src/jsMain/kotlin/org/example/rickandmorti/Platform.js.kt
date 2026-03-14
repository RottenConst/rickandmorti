package org.example.rickandmorti

import kotlin.js.Date

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun currentMillis(): Long = Date.now().toLong()