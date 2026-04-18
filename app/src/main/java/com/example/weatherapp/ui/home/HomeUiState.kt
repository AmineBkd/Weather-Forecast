package com.example.weatherapp.ui.home

import com.example.weatherapp.data.model.WeatherResponse

data class HomeUiState(
    val weatherData: Map<Int, WeatherResponse> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)