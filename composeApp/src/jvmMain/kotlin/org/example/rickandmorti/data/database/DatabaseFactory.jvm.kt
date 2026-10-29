package org.example.rickandmorti.data.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual fun createDatabase(): AppDatabase {
    val dbPath = "${System.getProperty("user.home")}/rickmorty.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbPath
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}