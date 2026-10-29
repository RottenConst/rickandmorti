package org.example.rickandmorti

import android.app.Application
import org.example.rickandmorti.shared.AppModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class RickAndMortyApplication : Application() {
    override fun onCreate(){
        super.onCreate()
        Platform2.initialize(this)

        startKoin {
            androidContext(this@RickAndMortyApplication)
            modules(
                module {
                    val settings = createSettings()
                    single<FavoritesStore> { SettingsFavoritesStore(settings) }

                    modules(AppModules.all())
                }
            )
        }
    }
}