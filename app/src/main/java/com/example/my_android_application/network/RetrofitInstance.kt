package com.example.my_android_application.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.4.1/"

    // Создаем OkHttpClient с кастомными таймаутами
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 30 секунд для подключения
        .readTimeout(30, TimeUnit.SECONDS)    // 30 секунд для чтения
        .writeTimeout(30, TimeUnit.SECONDS)   // 30 секунд для записи
        .build()

    // Настраиваем Retrofit с OkHttpClient
    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)                 // Добавляем кастомный клиент
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}