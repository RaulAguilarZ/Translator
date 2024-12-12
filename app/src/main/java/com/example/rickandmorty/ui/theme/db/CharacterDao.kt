package com.example.rickandmorty.ui.theme.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//methods

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Query("DELETE FROM character_table WHERE id = :characterId")
    suspend fun deleteCharacter(characterId: Int)
}
