package org.example.rickandmorti

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.browser.document
import org.example.rickandmorti.data.database.createDatabase
import org.example.rickandmorti.presentation.navigation.DefaultRootComponent
import org.example.rickandmorti.shared.AppModules
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val databaseFactory = createDatabase()

    startKoin {
        modules(
            module {
                single { databaseFactory }
                single<FavoritesStore> { SettingsFavoritesStore(createSettings()) }
                modules(AppModules.all())
            }
        )
    }

    ComposeViewport(document.body!!) {
        val rootComponent = DefaultRootComponent(
            DefaultComponentContext(LifecycleRegistry())
        )
        App(rootComponent)
    }
}