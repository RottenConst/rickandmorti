package org.example.rickandmorti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.window

actual fun openInBrowser(url: String) {
    window.open(url, "_blank")?.focus()
}

@OptIn(ExperimentalSettingsApi::class)
actual fun createSettings(): Settings {
    return StorageSettings()
}