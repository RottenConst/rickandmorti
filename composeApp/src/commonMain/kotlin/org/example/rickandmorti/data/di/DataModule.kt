package org.example.rickandmorti.data.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.rickandmorti.data.api.RickAndMortyApi
import org.example.rickandmorti.data.source.CharacterDataSource
import org.example.rickandmorti.data.source.EpisodeDataSource
import org.example.rickandmorti.data.source.LocationDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {

    single<Json> {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    //http client
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            defaultRequest {
                url("https://rickandmortyapi.com")
                this@HttpClient.expectSuccess = false
            }
        }
    }

    single { RickAndMortyApi(get()) }

    singleOf(::CharacterDataSource)
    singleOf(::EpisodeDataSource)
    singleOf(::LocationDataSource)
}