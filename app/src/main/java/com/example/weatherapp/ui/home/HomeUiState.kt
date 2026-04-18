package com.example.weatherapp.ui.home

import com.example.weatherapp.domain.model.Weather

data class HomeUiState(
    val weatherData: Map<Int, Weather> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)