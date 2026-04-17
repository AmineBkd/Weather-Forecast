package com.example.weatherapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val currentWeather: WeatherResponse? = null,
    val forecast: ForecastResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DetailViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun fetchDetails(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val currentDeferred = async { repository.getCurrentWeather(lat, lon) }
                val forecastDeferred = async { repository.getFiveDayForecast(lat, lon) }
                
                val current = currentDeferred.await()
                val forecast = forecastDeferred.await()
                
                _uiState.value = _uiState.value.copy(
                    currentWeather = current,
                    forecast = forecast,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to fetch details"
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: WeatherRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                        return DetailViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
