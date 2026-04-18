package com.example.weatherapp.domain.model

data class Weather(
    val cityName: String,
    val tempCelsius: Double,
    val feelsLikeCelsius: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val conditionMain: String,
    val conditionDescription: String,
    val iconCode: String,
    val timestampEpoch: Long
)
