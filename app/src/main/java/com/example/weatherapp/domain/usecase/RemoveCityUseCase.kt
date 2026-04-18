package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class RemoveCityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(city: City) {
        repository.removeCity(city)
    }
}
