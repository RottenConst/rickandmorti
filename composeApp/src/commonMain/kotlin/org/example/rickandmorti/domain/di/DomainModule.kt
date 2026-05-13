package org.example.rickandmorti.domain.di

import org.example.rickandmorti.data.repository.CharacterRepositoryImpl
import org.example.rickandmorti.domain.repository.CharacterRepository
import org.example.rickandmorti.domain.usecase.GetCharactersUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::CharacterRepositoryImpl) {
        bind<CharacterRepository>()
    }

    singleOf(::GetCharactersUseCase)
}