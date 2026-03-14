package org.example.rickandmorti

import androidx.compose.ui.window.ComposeUIViewController
import org.example.rickandmorti.di.appModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { App() }

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}