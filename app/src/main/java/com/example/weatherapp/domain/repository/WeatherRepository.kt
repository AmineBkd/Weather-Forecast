package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.Forecast
import com.example.weatherapp.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    val savedCities: Flow<List<City>>
    suspend fun addCity(city: City)
    suspend fun removeCity(city: City)
    suspend fun updateCurrentLocationCity(lat: Double, lon: Double)
    suspend fun ensureDefaultCities()
    suspend fun getCurrentWeather(lat: Double, lon: Double): Weather
    suspend fun getFiveDayForecast(lat: Double, lon: Double): Forecast
    suspend fun searchCity(query: String): List<CitySearchResult>
}