package com.example.my_android_application.network

import com.example.my_android_application.data.model.WifiData
import com.example.my_android_application.data.response.WifiServerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("wifi/connect")
    suspend fun connectToWiFi(@Body wifiData: WifiData): Response<WifiServerResponse>
}

