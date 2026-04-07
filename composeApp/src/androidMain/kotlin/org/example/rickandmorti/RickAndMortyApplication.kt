package org.example.rickandmorti

import android.app.Application

class RickAndMortyApplication : Application() {
    override fun onCreate(){
        super.onCreate()
        Platform2.initialize(this)
    }
}