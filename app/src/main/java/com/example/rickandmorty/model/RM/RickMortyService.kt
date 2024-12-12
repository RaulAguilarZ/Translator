package com.example.rickandmorty.model.RM

import retrofit2.Call
import retrofit2.http.GET

interface  RickMortyService{
    @GET("character")
    fun getCharacters(): Call<CharacterResponse>
}