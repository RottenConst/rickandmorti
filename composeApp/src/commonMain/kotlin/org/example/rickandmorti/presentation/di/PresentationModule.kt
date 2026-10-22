package org.example.rickandmorti.presentation.di

import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.presentation.EpisodesViewModel
import org.example.rickandmorti.presentation.LocationViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val presentationModule = module {
    factoryOf(::CharacterViewModel)
    factoryOf(::EpisodesViewModel)
    factoryOf(::LocationViewModel)
}