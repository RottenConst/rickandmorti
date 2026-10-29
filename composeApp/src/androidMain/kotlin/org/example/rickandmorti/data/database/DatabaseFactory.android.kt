package org.example.rickandmorti.data.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.example.rickandmorti.Platform2

actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(
        name = Platform2.context.getDatabasePath("rickmorty.db").absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}