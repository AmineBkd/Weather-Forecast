package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class AddCityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(name: String, lat: Double, lon: Double) {
        repository.addCity(City(name = name, lat = lat, lon = lon))
    }
}
