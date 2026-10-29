package org.example.rickandmorti.data.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import org.example.rickandmorti.data.dao.CharacterDao
import org.example.rickandmorti.data.dao.EpisodeDao
import org.example.rickandmorti.data.dao.LocationDao
import org.example.rickandmorti.data.entity.CharacterEntity
import org.example.rickandmorti.data.entity.EpisodeEntity
import org.example.rickandmorti.data.entity.LocationEntity

@Database(
    entities = [CharacterEntity::class, EpisodeEntity::class, LocationEntity::class],
    version = 2,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun locationDao(): LocationDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor: RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}