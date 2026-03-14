package org.example.rickandmorti

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.rickandmorti.di.appModule
import org.koin.core.context.startKoin

fun main() = application {

    startKoin {
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "rickandmorti",
    ) {
        App()
    }
}