package com.example.rickandmorty.model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ServiceDictionary {
    @POST("translate")
    suspend fun translateText(
        @Query("api-version") apiVersion: String = "3.0",
        @Query("to") to: String = "en",
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Ocp-Apim-Subscription-Key") subscriptionKey: String = "ATOogKd6zVShhgdFHO7hKwMHC3y3RVHTTaGpUyE4hEftqTJor0dFJQQJ99AKACBsN54XJ3w3AAAbACOGA5X5",
        @Header("Ocp-Apim-Subscription-Region") region: String = "canadacentral",

        @Body text: List<TextRequestBody>
    ): Response<List<TranslationResponse>>
}
