package com.example.weatherapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.usecase.AddCityUseCase
import com.example.weatherapp.domain.usecase.EnsureDefaultCitiesUseCase
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.domain.usecase.GetSavedCitiesUseCase
import com.example.weatherapp.domain.usecase.RemoveCityUseCase
import com.example.weatherapp.domain.usecase.SearchCityUseCase
import com.example.weatherapp.domain.usecase.UpdateCurrentLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSavedCities: GetSavedCitiesUseCase,
    private val getCurrentWeather: GetCurrentWeatherUseCase,
    private val addCity: AddCityUseCase,
    private val removeCity: RemoveCityUseCase,
    private val searchCity: SearchCityUseCase,
    private val updateCurrentLocation: UpdateCurrentLocationUseCase,
    private val ensureDefaultCities: EnsureDefaultCitiesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val savedCities = getSavedCities()

    init {
        viewModelScope.launch { ensureDefaultCities() }
    }

    fun fetchWeatherDataForCities(cities: List<City>, forceRefresh: Boolean = false) {
        if (cities.isEmpty()) return
        viewModelScope.launch {
            if (forceRefresh || uiState.value.weatherData.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            try {
                val currentData = _uiState.value.weatherData.toMutableMap()
                var dataChanged = false

                for (city in cities) {
                    if (forceRefresh || !currentData.containsKey(city.id)) {
                        currentData[city.id] = getCurrentWeather(city.lat, city.lon)
                        dataChanged = true
                    }
                }

                if (dataChanged || forceRefresh) {
                    _uiState.value = _uiState.value.copy(
                        weatherData = currentData,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred fetching weather data"
                )
            }
        }
    }

    fun addCity(name: String, lat: Double, lon: Double) {
        viewModelScope.launch { addCity.invoke(name, lat, lon) }
    }

    fun removeCity(city: City) {
        viewModelScope.launch { removeCity.invoke(city) }
    }

    fun addCurrentLocation(lat: Double, lon: Double) {
        viewModelScope.launch { updateCurrentLocation(lat, lon) }
    }

    suspend fun searchCity(query: String): List<CitySearchResult> = searchCity.invoke(query)
}
