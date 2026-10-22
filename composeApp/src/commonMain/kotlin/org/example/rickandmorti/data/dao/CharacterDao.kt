package org.example.rickandmorti.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.data.entity.CharacterEntity

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterFlow(id: Int): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?

    @Query("SELECT * FROM characters WHERE name LIKE '%' || :name || '%'")
    fun searchCharacters(name: String): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun deleteAll()

    @Query("DELETE FROM characters WHERE id IN (:ids)")
    suspend fun deleteCharactersByIds(ids: List<Int>)
}