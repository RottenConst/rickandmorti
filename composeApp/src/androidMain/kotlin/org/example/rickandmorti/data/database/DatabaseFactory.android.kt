package org.example.rickandmorti.data.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.example.rickandmorti.Platform2

lateinit var context: Context
actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(
        name = Platform2.context.getDatabasePath("rickmorty.db").absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}