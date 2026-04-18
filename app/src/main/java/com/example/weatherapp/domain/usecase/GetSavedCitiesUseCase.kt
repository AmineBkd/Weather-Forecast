package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedCitiesUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(): Flow<List<City>> = repository.savedCities
}
