package com.example.weatherapp.domain.model

data class CitySearchResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)
