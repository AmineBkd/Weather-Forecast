package com.example.weatherapp.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weatherapp.domain.model.ForecastItem
import com.example.weatherapp.domain.model.Weather
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    cityName: String,
    lat: Double,
    lon: Double,
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDetails(lat, lon)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cityName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val current = uiState.currentWeather
                val forecast = uiState.forecast

                if (current != null && forecast != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { CurrentWeatherHeader(weather = current) }
                        item { WeatherDetailsGrid(weather = current) }
                        item {
                            Text(
                                "5-Day Forecast",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        val dailyForecasts = forecast.items
                            .filter { it.dateText.contains("12:00:00") }
                            .takeIf { it.isNotEmpty() } ?: forecast.items.take(5)

                        items(dailyForecasts) { item ->
                            ForecastItemRow(forecastItem = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherHeader(weather: Weather) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (weather.iconCode.isNotEmpty()) {
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${weather.iconCode}@4x.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(120.dp)
            )
        }
        Text(
            text = "${weather.tempCelsius.toInt()}°C",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = weather.conditionDescription.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun WeatherDetailsGrid(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailItem("Humidity", "${weather.humidity}%")
            DetailItem("Wind", "${weather.windSpeed} m/s")
            DetailItem("Pressure", "${weather.pressure} hPa")
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastItemRow(forecastItem: ForecastItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val date = Date(forecastItem.timestampEpoch * 1000)
            val formatDay = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

            Text(
                text = formatDay.format(date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${forecastItem.tempCelsius.toInt()}°C",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (forecastItem.iconCode.isNotEmpty()) {
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${forecastItem.iconCode}@2x.png",
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
