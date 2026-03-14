package org.example.rickandmorti.di

import org.example.rickandmorti.GreetingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    factory { (initial: String) ->
        GreetingViewModel(initial)
    }
}