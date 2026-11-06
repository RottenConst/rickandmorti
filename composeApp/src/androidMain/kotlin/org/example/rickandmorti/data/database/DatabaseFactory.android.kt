package org.example.rickandmorti.data.database

import androidx.room3.Room
import kotlinx.coroutines.Dispatchers
import org.example.rickandmorti.Platform2

actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(
        context = Platform2.context,
        name = Platform2.context.getDatabasePath("rickmorty.db").absolutePath,
    )
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}