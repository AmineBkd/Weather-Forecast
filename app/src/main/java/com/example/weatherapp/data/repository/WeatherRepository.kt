package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.api.OpenWeatherApi
import com.example.weatherapp.data.db.CityDao
import com.example.weatherapp.data.db.CityEntity
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.GeocodingResponseItem
import com.example.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val api: OpenWeatherApi,
    private val dao: CityDao
) {
    private val apiKey = BuildConfig.WEATHER_API_KEY

    // Room operations
    val savedCities: Flow<List<CityEntity>> = dao.getAllCities()

    suspend fun insertCity(city: CityEntity) {
        withContext(Dispatchers.IO) {
            dao.insertCity(city)
        }
    }

    suspend fun deleteCity(city: CityEntity) {
        withContext(Dispatchers.IO) {
            dao.deleteCity(city)
        }
    }

    suspend fun updateCurrentLocationCity(lat: Double, lon: Double) {
        val reverseGeocodeResults = try {
            api.reverseGeocode(lat = lat, lon = lon, apiKey = apiKey)
        } catch (e: Exception) {
            emptyList()
        }
        val cityName = reverseGeocodeResults.firstOrNull()?.name ?: "Current Location"
        
        withContext(Dispatchers.IO) {
            dao.deleteCurrentLocationCity()
            dao.insertCity(
                CityEntity(
                    name = cityName,
                    lat = lat,
                    lon = lon,
                    isCurrentLocation = true
                )
            )
        }
    }

    suspend fun ensureDefaultCities() {
        withContext(Dispatchers.IO) {
            if (dao.getCitiesCount() == 0) {
                val defaults = listOf(
                    "Casablanca" to Pair(33.5731, -7.5898),
                    "Rabat" to Pair(34.0209, -6.8416),
                    "Marrakech" to Pair(31.6295, -7.9811),
                    "Tangier" to Pair(35.7595, -5.8340),
                    "Fes" to Pair(34.0331, -5.0003)
                )
                defaults.forEach { (name, coords) ->
                    insertCity(CityEntity(name = name, lat = coords.first, lon = coords.second))
                }
            }
        }
    }

    // Network operations
    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getCurrentWeather(lat = lat, lon = lon, apiKey = apiKey)
    }

    suspend fun getFiveDayForecast(lat: Double, lon: Double): ForecastResponse {
        return api.getFiveDayForecast(lat = lat, lon = lon, apiKey = apiKey)
    }

    suspend fun searchCity(query: String): List<GeocodingResponseItem> {
        return api.geocodeCity(cityName = query, apiKey = apiKey)
    }
}
