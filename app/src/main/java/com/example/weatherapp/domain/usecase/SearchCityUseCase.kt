package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class SearchCityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(query: String): List<CitySearchResult> =
        repository.searchCity(query)
}
