package org.example.rickandmorti


class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun openInBrowser(url: String) {
    js("window.open(url, '_blank')")
}