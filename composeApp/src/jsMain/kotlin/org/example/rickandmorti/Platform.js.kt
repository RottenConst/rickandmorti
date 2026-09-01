package org.example.rickandmorti

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual fun openInBrowser(url: String) {
    js("window.open(url, '_blank')")
}

actual fun createSettings(): Settings {
    return StorageSettings()
}