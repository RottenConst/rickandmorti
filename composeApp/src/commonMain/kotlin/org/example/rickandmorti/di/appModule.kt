package org.example.rickandmorti.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.GreetingRepository
import org.example.rickandmorti.GreetingViewModel
import org.example.rickandmorti.SettingsFavoritesStore
import org.example.rickandmorti.createSettings
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<FavoritesStore> {
        SettingsFavoritesStore(createSettings())
    }
    // Единственный экземпляр репозитория
    singleOf(::GreetingRepository)

    // Фабрика для создания отдельного CoroutineScope (на каждый запрос)
    factory(named("ViewModelScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    // Фабрика GreetingViewModel
    factory { (initialGreeting: String) ->
        GreetingViewModel(
            coroutineScope = get(named("ViewModelScope")),
            greetingRepository = get(),
            initialGreeting = initialGreeting
        )
    }
}