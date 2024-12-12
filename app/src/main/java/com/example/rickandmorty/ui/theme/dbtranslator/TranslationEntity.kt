package com.example.rickandmorty.ui.theme.dbtranslator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations_table")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val textEnglish: String, // Text in English
    val textSpanish: String, // Spanish
    val language: String = "en", // Default language is "en"
    val country: String = "MX", // Default country is "MX"
    // val date: Long = System.currentTimeMillis(), // Default is current system time (timestamp)
    val category: String = "Restaurants" // Default status is "Great"
)