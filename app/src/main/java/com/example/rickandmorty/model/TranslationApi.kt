package com.example.rickandmorty.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TranslationApi {
    private const val BASE_URL = "https://api.cognitive.microsofttranslator.com/"

    val service: ServiceDictionary by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            //.client(client)
            .build()
            .create(ServiceDictionary::class.java)
    }
}
