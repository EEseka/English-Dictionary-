package com.example.dictionary.data.remote

import retrofit2.http.GET

interface RandomWordApi {

    @GET("/word")
    suspend fun getRandomWord(): List<String>

    companion object {
        const val BASE_URL = "https://random-word-api.herokuapp.com/"
    }
}