package com.example.rickandmorty.ui.theme.dbtranslator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)

    suspend fun insertTranslation(translation: TranslationEntity)

    @Query("DELETE FROM translations_table WHERE id = :translationId")
    suspend fun deleteTranslation(translationId: Int)

    @Update
    suspend fun updateTranslation(translation: TranslationEntity)

    @Query("SELECT * FROM translations_table")
    suspend fun getAllTranslations(): List<TranslationEntity>
}