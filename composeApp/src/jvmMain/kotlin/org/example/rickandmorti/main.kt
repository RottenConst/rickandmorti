package org.example.rickandmorti

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.start
import org.example.rickandmorti.di.appModule
import org.example.rickandmorti.navigation.DefaultRootComponent
import org.koin.core.context.startKoin

fun main() {

    application {

        val lifecycle = LifecycleRegistry()

        startKoin {
            modules(appModule)
        }

        val rootComponent = DefaultRootComponent(DefaultComponentContext(lifecycle))

        lifecycle.start()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Rick and Morty",
        ) {
            App(rootComponent)
        }
    }
}