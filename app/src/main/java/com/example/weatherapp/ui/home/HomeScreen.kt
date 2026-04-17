package com.example.weatherapp.ui.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.data.db.CityEntity
import com.example.weatherapp.util.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (lat: Double, lon: Double, cityName: String) -> Unit
) {
    val savedCities by viewModel.savedCities.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showSearchDialog by remember { mutableStateOf(false) }
    
    val pullToRefreshState = rememberPullToRefreshState()

    // Fetch data initially if list is populated
    LaunchedEffect(savedCities) {
        if (savedCities.isNotEmpty() && uiState.weatherData.isEmpty()) {
            viewModel.fetchWeatherDataForCities(savedCities)
        }
    }

    val locationHelper = remember { LocationHelper(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            scope.launch {
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    viewModel.addCurrentLocation(location.first, location.second)
                } else {
                    Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Forecast", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Current Location")
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search City")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSearchDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add City")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.fetchWeatherDataForCities(savedCities) },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savedCities, key = { it.id }) { city ->
                        val weather = uiState.weatherData[city.id]
                        CityWeatherCard(
                            city = city,
                            weather = weather,
                            onClick = { onNavigateToDetail(city.lat, city.lon, city.name) },
                            onDelete = { viewModel.removeCity(city) }
                        )
                    }
                }
            }
        }
    }

    if (showSearchDialog) {
        SearchDialog(
            viewModel = viewModel,
            onDismiss = { showSearchDialog = false },
            onCitySelected = { lat, lon, name ->
                viewModel.addCity(name, lat, lon)
                showSearchDialog = false
            }
        )
    }
}

@Composable
fun CityWeatherCard(
    city: CityEntity,
    weather: com.example.weatherapp.data.model.WeatherResponse?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (city.isCurrentLocation) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Current Location",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (weather != null) {
                    val desc = weather.weather.firstOrNull()?.main ?: "Unknown"
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (weather != null) {
                val iconIcon = weather.weather.firstOrNull()?.icon
                if (iconIcon != null) {
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/$iconIcon@2x.png",
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "${weather.main.temp.toInt()}°C",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove City",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SearchDialog(
    viewModel: HomeViewModel,
    onDismiss: () -> Unit,
    onCitySelected: (Double, Double, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<com.example.weatherapp.data.model.GeocodingResponseItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var isSearching by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search City") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("City Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isSearching = true
                            try {
                                results = viewModel.searchCity(query)
                            } catch (e: Exception) {
                                // handle error natively
                            } finally {
                                isSearching = false
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Search")
                }
                
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(results) { item ->
                        val locationName = "${item.name}, ${item.state ?: ""} ${item.country}"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(item.lat, item.lon, item.name) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = locationName, style = MaterialTheme.typography.bodyLarge)
                        }
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
