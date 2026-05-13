package org.example.rickandmorti.shared

import org.example.rickandmorti.data.di.dataModule
import org.example.rickandmorti.domain.di.domainModule
import org.example.rickandmorti.presentation.di.presentationModule
import org.koin.core.module.Module

object AppModules {
    fun platformModules(): List<Module> = emptyList()
    fun all(): List<Module> = platformModules() + listOf(
        dataModule, domainModule, presentationModule
    )
}