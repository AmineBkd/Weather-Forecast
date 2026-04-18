package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.api.OpenWeatherApi
import com.example.weatherapp.data.db.CityDao
import com.example.weatherapp.data.mapper.toDomain
import com.example.weatherapp.data.mapper.toEntity
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.Forecast
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenWeatherApi,
    private val dao: CityDao
) : WeatherRepository {

    private val apiKey = BuildConfig.WEATHER_API_KEY

    override val savedCities: Flow<List<City>> =
        dao.getAllCities().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addCity(city: City) {
        withContext(Dispatchers.IO) { dao.insertCity(city.toEntity()) }
    }

    override suspend fun removeCity(city: City) {
        withContext(Dispatchers.IO) { dao.deleteCity(city.toEntity()) }
    }

    override suspend fun updateCurrentLocationCity(lat: Double, lon: Double) {
        val cityName = try {
            api.reverseGeocode(lat = lat, lon = lon, apiKey = apiKey)
                .firstOrNull()?.name ?: "Current Location"
        } catch (e: Exception) {
            "Current Location"
        }
        withContext(Dispatchers.IO) {
            dao.deleteCurrentLocationCity()
            dao.insertCity(
                com.example.weatherapp.data.db.CityEntity(
                    name = cityName,
                    lat = lat,
                    lon = lon,
                    isCurrentLocation = true
                )
            )
        }
    }

    override suspend fun ensureDefaultCities() {
        withContext(Dispatchers.IO) {
            if (dao.getCitiesCount() == 0) {
                listOf(
                    "Casablanca" to Pair(33.5731, -7.5898),
                    "Rabat"      to Pair(34.0209, -6.8416),
                    "Marrakech"  to Pair(31.6295, -7.9811),
                    "Tangier"    to Pair(35.7595, -5.8340),
                    "Fes"        to Pair(34.0331, -5.0003)
                ).forEach { (name, coords) ->
                    dao.insertCity(
                        com.example.weatherapp.data.db.CityEntity(
                            name = name,
                            lat = coords.first,
                            lon = coords.second
                        )
                    )
                }
            }
        }
    }

    // ── Network calls (API → domain) ──────────────────────────────────────

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Weather =
        api.getCurrentWeather(lat = lat, lon = lon, apiKey = apiKey).toDomain()

    override suspend fun getFiveDayForecast(lat: Double, lon: Double): Forecast =
        api.getFiveDayForecast(lat = lat, lon = lon, apiKey = apiKey).toDomain()

    override suspend fun searchCity(query: String): List<CitySearchResult> =
        api.geocodeCity(cityName = query, apiKey = apiKey).map { it.toDomain() }
}
