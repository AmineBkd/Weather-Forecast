package com.example.weatherapp.di

import android.content.Context
import com.example.weatherapp.data.api.OpenWeatherApi
import com.example.weatherapp.data.db.AppDatabase
import com.example.weatherapp.data.repository.WeatherRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: OpenWeatherApi by lazy {
        retrofit.create(OpenWeatherApi::class.java)
    }

    private val database = AppDatabase.getDatabase(context)

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(api, database.cityDao())
    }
}
