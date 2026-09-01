package org.example.rickandmorti

import com.russhwolf.settings.Settings

interface Platform {
    val name: String
}

expect fun openInBrowser(url :String)

expect fun createSettings(): Settings