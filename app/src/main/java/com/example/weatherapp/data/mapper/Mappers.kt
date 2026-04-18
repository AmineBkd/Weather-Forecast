package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.db.CityEntity
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.GeocodingResponseItem
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.Forecast
import com.example.weatherapp.domain.model.ForecastItem
import com.example.weatherapp.domain.model.Weather

// ── CityEntity ↔ Domain ────────────────────────────────────────────────────

fun CityEntity.toDomain(): City = City(
    id = id,
    name = name,
    lat = lat,
    lon = lon,
    isCurrentLocation = isCurrentLocation
)

fun City.toEntity(): CityEntity = CityEntity(
    id = id,
    name = name,
    lat = lat,
    lon = lon,
    isCurrentLocation = isCurrentLocation
)

// ── WeatherResponse → Domain ───────────────────────────────────────────────

fun WeatherResponse.toDomain(): Weather {
    val condition = weather.firstOrNull()
    return Weather(
        cityName = name,
        tempCelsius = main.temp,
        feelsLikeCelsius = main.feelsLike,
        tempMin = main.tempMin,
        tempMax = main.tempMax,
        humidity = main.humidity,
        pressure = main.pressure,
        windSpeed = wind.speed,
        windDeg = wind.deg,
        conditionMain = condition?.main ?: "",
        conditionDescription = condition?.description ?: "",
        iconCode = condition?.icon ?: "",
        timestampEpoch = dt
    )
}

// ── ForecastResponse → Domain ──────────────────────────────────────────────

fun ForecastResponse.toDomain(): Forecast = Forecast(
    cityName = city.name,
    items = list.map { dto ->
        val condition = dto.weather.firstOrNull()
        ForecastItem(
            timestampEpoch = dto.dt,
            dateText = dto.dtTxt,
            tempCelsius = dto.main.temp,
            conditionMain = condition?.main ?: "",
            conditionDescription = condition?.description ?: "",
            iconCode = condition?.icon ?: ""
        )
    }
)

// ── GeocodingResponseItem → Domain ────────────────────────────────────────

fun GeocodingResponseItem.toDomain(): CitySearchResult = CitySearchResult(
    name = name,
    lat = lat,
    lon = lon,
    country = country,
    state = state
)
