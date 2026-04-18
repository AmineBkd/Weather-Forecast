package com.example.weatherapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.db.CityEntity
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val weatherData: Map<Int, WeatherResponse> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val savedCities = repository.savedCities

    init {
        viewModelScope.launch {
            repository.ensureDefaultCities()
        }
    }

    fun fetchWeatherDataForCities(cities: List<CityEntity>, forceRefresh: Boolean = false) {
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
                        val response = repository.getCurrentWeather(lat = city.lat, lon = city.lon)
                        currentData[city.id] = response
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
        viewModelScope.launch {
            repository.insertCity(
                CityEntity(name = name, lat = lat, lon = lon)
            )
        }
    }

    fun removeCity(city: CityEntity) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
    }

    fun addCurrentLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.updateCurrentLocationCity(lat, lon)
        }
    }

    suspend fun searchCity(query: String) = repository.searchCity(query)

    // ViewModel Factory
    companion object {
        fun provideFactory(repository: WeatherRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        return HomeViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
