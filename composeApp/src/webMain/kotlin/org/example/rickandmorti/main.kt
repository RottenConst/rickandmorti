package org.example.rickandmorti

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.rickandmorti.presentation.navigation.DefaultRootComponent
import org.example.rickandmorti.shared.AppModules
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(
            module {
                val setting = createSettings()
                single<FavoritesStore> { SettingsFavoritesStore(setting) }
                factory(named("ViewModelScope")) {
                    CoroutineScope(SupervisorJob() + Dispatchers.Main)
                }
                modules(AppModules.all())
            }
        )
    }

    ComposeViewport {
        val rootComponent = DefaultRootComponent(DefaultComponentContext(lifecycle = LifecycleRegistry()))
        App(rootComponent)
    }
}