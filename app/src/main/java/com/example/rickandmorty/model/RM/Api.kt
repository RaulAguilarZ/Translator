package com.example.rickandmorty.model.RM

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Api {
    private val Base_URL = "https://rickandmortyapi.com/api/"

    // convert json into objects
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // initialize retrofit
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(Base_URL)
        .build()

    val retrofitService: RickMortyService by lazy {
        retrofit.create(RickMortyService::class.java)
    }
}