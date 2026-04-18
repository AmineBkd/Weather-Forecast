package com.example.weatherapp.domain.model

data class City(
    val id: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean = false
)
