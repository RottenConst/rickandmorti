package org.example.rickandmorti

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "rickandmorti",
    ) {
        App()
    }
}