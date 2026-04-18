package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class UpdateCurrentLocationUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) {
        repository.updateCurrentLocationCity(lat, lon)
    }
}
