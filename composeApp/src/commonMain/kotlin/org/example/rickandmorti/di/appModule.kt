package org.example.rickandmorti.di

import org.example.rickandmorti.GreetingRepository
import org.example.rickandmorti.GreetingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::GreetingRepository)
    factory { (initial: String) ->
        GreetingViewModel(initial, get())
    }
}