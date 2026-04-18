package com.example.weatherapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.domain.usecase.GetForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCurrentWeather: GetCurrentWeatherUseCase,
    private val getForecast: GetForecastUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun fetchDetails(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val weatherDeferred = async { getCurrentWeather(lat, lon) }
                val forecastDeferred = async { getForecast(lat, lon) }

                _uiState.value = _uiState.value.copy(
                    currentWeather = weatherDeferred.await(),
                    forecast = forecastDeferred.await(),
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
}
