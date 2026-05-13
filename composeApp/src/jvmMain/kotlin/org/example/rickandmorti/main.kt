package org.example.rickandmorti

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.start
import org.example.rickandmorti.presentation.navigation.DefaultRootComponent
import org.example.rickandmorti.shared.AppModules
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

fun main() {

    application {

        val lifecycle = LifecycleRegistry()

        GlobalContext.startKoin {
            modules(
                module {
                    val settings = createSettings()
                    single<FavoritesStore> { SettingsFavoritesStore(settings) }
                    modules(AppModules.all())
                }
            )
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