package com.example.my_android_application.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import io.github.cdimascio.dotenv.dotenv

object RetrofitInstance {
    private val dotenv = dotenv {
        ignoreIfMissing = true // Игнорировать, если .env отсутствует
    }
    private val BASE_URL = System.getenv("BASE_URL") ?: dotenv["BASE_URL"] ?: "http://example.com/"

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}