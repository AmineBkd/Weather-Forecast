package com.example.weatherapp.domain.model

data class Forecast(
    val cityName: String,
    val items: List<ForecastItem>
)

data class ForecastItem(
    val timestampEpoch: Long,
    val dateText: String,
    val tempCelsius: Double,
    val conditionMain: String,
    val conditionDescription: String,
    val iconCode: String
)
