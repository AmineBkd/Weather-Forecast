package com.example.weatherapp.ui.detail

import com.example.weatherapp.domain.model.Forecast
import com.example.weatherapp.domain.model.Weather

data class DetailUiState(
    val currentWeather: Weather? = null,
    val forecast: Forecast? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)