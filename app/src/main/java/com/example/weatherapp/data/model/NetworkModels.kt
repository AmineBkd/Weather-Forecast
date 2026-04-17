package com.example.weatherapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val weather: List<WeatherDto>,
    val main: MainDto,
    val wind: WindDto,
    val name: String,
    val dt: Long
)

@JsonClass(generateAdapter = true)
data class WeatherDto(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class MainDto(
    val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

@JsonClass(generateAdapter = true)
data class WindDto(
    val speed: Double,
    val deg: Int
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    val list: List<ForecastDto>,
    val city: CityDto
)

@JsonClass(generateAdapter = true)
data class ForecastDto(
    val dt: Long,
    val main: MainDto,
    val weather: List<WeatherDto>,
    val wind: WindDto,
    @Json(name = "dt_txt") val dtTxt: String
)

@JsonClass(generateAdapter = true)
data class CityDto(
    val id: Int,
    val name: String,
    val coord: CoordDto,
    val country: String,
    val timezone: Int
)

@JsonClass(generateAdapter = true)
data class CoordDto(
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class GeocodingResponseItem(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)
