package org.example.rickandmorti

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()
private const val SETTINGS_NODE = "/org/example/rickandmorti"

actual fun createSettings(): ObservableSettings {
    val node = Preferences.userRoot().node(SETTINGS_NODE)
    return PreferencesSettings(node)
}