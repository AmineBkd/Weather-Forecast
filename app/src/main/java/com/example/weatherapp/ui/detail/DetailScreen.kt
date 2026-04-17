package com.example.weatherapp.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        item {
                            CurrentWeatherHeader(current = current)
                        }
                        item {
                            WeatherDetailsGrid(current = current)
                        }
                        item {
                            Text(
                                "5-Day Forecast",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Group forecast by day to show one item per day or leave as a 3-hour list
                        // The prompt asks for 5 day weather forecast. We can just show the list.
                        val dailyForecasts = forecast.list.filter { it.dtTxt.contains("12:00:00") }.takeIf { it.isNotEmpty() } ?: forecast.list.take(5)

                        items(dailyForecasts) { f ->
                            ForecastItemRow(forecastItem = f)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherHeader(current: com.example.weatherapp.data.model.WeatherResponse) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val iconItem = current.weather.firstOrNull()?.icon
        if (iconItem != null) {
            AsyncImage(
                model = "https://openweathermap.org/img/wn/$iconItem@4x.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(120.dp)
            )
        }
        Text(
            text = "${current.main.temp.toInt()}°C",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun WeatherDetailsGrid(current: com.example.weatherapp.data.model.WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailItem("Humidity", "${current.main.humidity}%")
            DetailItem("Wind", "${current.wind.speed} m/s")
            DetailItem("Pressure", "${current.main.pressure} hPa")
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
fun ForecastItemRow(forecastItem: com.example.weatherapp.data.model.ForecastDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val date = Date(forecastItem.dt * 1000)
            val formatDay = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
            
            Text(
                text = formatDay.format(date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${forecastItem.main.temp.toInt()}°C",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                val iconIcon = forecastItem.weather.firstOrNull()?.icon
                if (iconIcon != null) {
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/$iconIcon@2x.png",
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
