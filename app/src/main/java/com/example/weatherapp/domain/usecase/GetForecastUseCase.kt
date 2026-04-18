package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.Forecast
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Forecast =
        repository.getFiveDayForecast(lat, lon)
}
