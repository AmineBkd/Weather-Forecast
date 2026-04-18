package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class EnsureDefaultCitiesUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke() {
        repository.ensureDefaultCities()
    }
}
