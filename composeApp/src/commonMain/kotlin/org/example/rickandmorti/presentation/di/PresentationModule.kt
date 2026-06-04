package org.example.rickandmorti.presentation.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.presentation.EpisodesViewModel
import org.example.rickandmorti.presentation.LocationViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val presentationModule = module {
    factory(named("ViewModelScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    factoryOf(::CharacterViewModel)
    factoryOf(::EpisodesViewModel)
    factoryOf(::LocationViewModel)
}