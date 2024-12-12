package com.example.rickandmorty.model.RM


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CharacterResponse(
    @Json(name = "info")
    val info: Info?,
    @Json(name = "results")
    val results: List<Result>?
)
