package org.example.rickandmorti

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()
actual fun currentTimeMillis(): Long = currentTimeMillis()