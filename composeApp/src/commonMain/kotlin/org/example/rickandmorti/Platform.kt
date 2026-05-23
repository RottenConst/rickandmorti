package org.example.rickandmorti

import com.russhwolf.settings.ObservableSettings

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openInBrowser(url :String)

expect fun createSettings(): ObservableSettings