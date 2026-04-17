package com.example.weatherapp

import com.example.weatherapp.data.api.OpenWeatherApi
import com.example.weatherapp.data.db.CityDao
import com.example.weatherapp.data.db.CityEntity
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.GeocodingResponseItem
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WeatherRepositoryTest {

    private val fakeApi = object : OpenWeatherApi {
        override suspend fun getCurrentWeather(
            lat: Double,
            lon: Double,
            apiKey: String,
            units: String
        ): WeatherResponse {
            // Simplified Fake Response
            return WeatherResponse(
                weather = emptyList(),
                main = com.example.weatherapp.data.model.MainDto(20.0, 20.0, 20.0, 20.0, 1013, 50),
                wind = com.example.weatherapp.data.model.WindDto(5.0, 180),
                name = "Fake City",
                dt = 123456789
            )
        }

        override suspend fun getFiveDayForecast(
            lat: Double,
            lon: Double,
            apiKey: String,
            units: String
        ): ForecastResponse {
            return ForecastResponse(emptyList(), com.example.weatherapp.data.model.CityDto(1, "Fake", com.example.weatherapp.data.model.CoordDto(0.0,0.0), "AA", 0))
        }

        override suspend fun geocodeCity(
            cityName: String,
            limit: Int,
            apiKey: String
        ): List<GeocodingResponseItem> {
            return listOf(GeocodingResponseItem("Fake City", 0.0, 0.0, "AA"))
        }

    }

    private val fakeDao = object : CityDao {
        private val list = mutableListOf<CityEntity>()
        override fun getAllCities(): Flow<List<CityEntity>> = flowOf(list)
        override fun insertCity(city: CityEntity) { list.add(city) }
        override fun deleteCity(city: CityEntity) { list.remove(city) }
        override fun deleteCurrentLocationCity() {}
        override fun getCitiesCount(): Int = list.size
    }

    @Test
    fun testGetCurrentWeather() = runBlocking {
        val repo = WeatherRepository(fakeApi, fakeDao)
        val data = repo.getCurrentWeather(0.0, 0.0)
        assertEquals("Fake City", data.name)
        assertEquals(20.0, data.main.temp, 0.0)
    }

    @Test
    fun testEnsureDefaultCities() = runBlocking {
        val repo = WeatherRepository(fakeApi, fakeDao)
        val countBefore = fakeDao.getCitiesCount()
        assertEquals(0, countBefore)
        
        repo.ensureDefaultCities()
        
        val countAfter = fakeDao.getCitiesCount()
        assertEquals(5, countAfter) // Casablanca, Rabat, Marrakech, Tangier, Fes
    }
}
