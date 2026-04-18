package com.example.weatherapp.ui.detail

import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.WeatherResponse


data class DetailUiState(
    val currentWeather: WeatherResponse? = null,
    val forecast: ForecastResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)