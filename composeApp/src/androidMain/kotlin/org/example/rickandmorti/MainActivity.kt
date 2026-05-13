package org.example.rickandmorti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.arkivanov.decompose.defaultComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.rickandmorti.shared.AppModules
import org.example.rickandmorti.presentation.navigation.DefaultRootComponent
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(
                module {
                    val settings = createSettings()
                    single<FavoritesStore> { SettingsFavoritesStore(settings) }

                    // Фабрика для создания CoroutineScope (на каждый запрос — новый scope)
                    factory(named("ViewModelScope")) {
                        CoroutineScope(SupervisorJob() + Dispatchers.Main)
                    }
                    modules(AppModules.all())
                }
            )
        }

        setContent {
            MaterialTheme {
                val rootComponent = DefaultRootComponent(defaultComponentContext())
                App(rootComponent)
            }
        }
    }
}