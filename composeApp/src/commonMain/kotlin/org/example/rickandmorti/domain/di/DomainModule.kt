package org.example.rickandmorti.domain.di

import org.example.rickandmorti.data.repository.CharacterRepositoryImpl
import org.example.rickandmorti.data.repository.EpisodeRepositoryImpl
import org.example.rickandmorti.data.repository.LocationRepositoryImpl
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.domain.repository.EpisodeRepository
import org.example.rickandmorti.domain.repository.LocationRepository
import org.example.rickandmorti.domain.usecase.GetCharacterByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeUseCase
import org.example.rickandmorti.domain.usecase.GetLocationByUrlUseCase
import org.example.rickandmorti.domain.usecase.GetLocationsUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::CharacterRepositoryImpl) {
        bind<CharacterRepository>()
    }

    singleOf(::EpisodeRepositoryImpl) {
        bind<EpisodeRepository>()
    }

    singleOf(::LocationRepositoryImpl) {
        bind<LocationRepository>()
    }

    singleOf(::GetCharactersUseCase)
    singleOf(::GetEpisodeUseCase)
    singleOf(::GetLocationsUseCase)
    singleOf(::GetCharacterByUrlUseCase)
    singleOf(::GetEpisodeByUrlUseCase)
    singleOf(::GetLocationByUrlUseCase)
}