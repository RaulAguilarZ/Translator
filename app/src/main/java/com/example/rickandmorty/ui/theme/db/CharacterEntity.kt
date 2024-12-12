package com.example.rickandmorty.ui.theme.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// This defines a database entity for storing character information.
@Entity(tableName = "character_table")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val gender: String,
    val origin: String,
    val image: String
)

