package org.example.rickandmorti.domain.di

import org.example.rickandmorti.data.repository.CharacterRepositoryImpl
import org.example.rickandmorti.data.repository.EpisodeRepositoryImpl
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.domain.repository.EpisodeRepository
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.example.rickandmorti.domain.usecase.GetEpisodeByUrlUseCase
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

    singleOf(::GetCharactersUseCase)
    singleOf(::GetEpisodeByUrlUseCase)
}