package org.example.rickandmorti.data.database

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun createDatabase(): AppDatabase {
    val worker = Worker("sqlite.worker.js")

    return Room.databaseBuilder<AppDatabase>(
        name = "rickmorty.db"
    ).setDriver(WebWorkerSQLiteDriver(worker)).build()
}