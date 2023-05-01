package com.example.rutacasa

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://api.openrouteservice.org"
private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private val retrofit =
    Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshi)).baseUrl(BASE_URL)
        .build()

interface ApiService {
    @GET("v2/directionsfoot-walking")
    suspend fun getRoute(
        @Query("api_key") key: String,
        @Query("start",encoded = true) start: String,
        @Query("end",encoded = true) end: String
    ): Coordinates
}

   object Directions {
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

