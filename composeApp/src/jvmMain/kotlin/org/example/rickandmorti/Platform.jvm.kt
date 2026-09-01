package org.example.rickandmorti

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.awt.Desktop
import java.net.URI
import java.util.prefs.Preferences

private const val SETTINGS_NODE = "/org/example/rickandmorti"

actual fun createSettings(): Settings {
    val node = Preferences.userRoot().node(SETTINGS_NODE)
    return PreferencesSettings(node)
}

actual fun openInBrowser(url: String) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(url))
    }
}