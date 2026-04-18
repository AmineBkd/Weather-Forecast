package com.example.weatherapp.ui.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
    var showAddCityDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Filtered list for the in-list search feature
    val filteredCities = remember(savedCities, searchQuery) {
        if (searchQuery.isBlank()) savedCities
        else savedCities.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    val pullToRefreshState = rememberPullToRefreshState()

    // Fetch data whenever savedCities changes (the ViewModel will only fetch missing ones unless forced)
    LaunchedEffect(savedCities) {
        if (savedCities.isNotEmpty()) {
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
            Column {
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
                        // Search within saved cities list
                        IconButton(onClick = {
                            showSearchBar = !showSearchBar
                            if (!showSearchBar) searchQuery = ""
                        }) {
                            Icon(
                                imageVector = if (showSearchBar) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = if (showSearchBar) "Close Search" else "Search Cities"
                            )
                        }
                        // Add a new city
                        IconButton(onClick = { showAddCityDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add City")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                // Inline search bar for filtering saved cities
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    LaunchedEffect(showSearchBar) {
                        if (showSearchBar) focusRequester.requestFocus()
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search saved cities…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.fetchWeatherDataForCities(savedCities, forceRefresh = true) },
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
                if (filteredCities.isEmpty() && savedCities.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No cities match \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredCities, key = { it.id }) { city ->
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
    }

    if (showAddCityDialog) {
        AddCityDialog(
            viewModel = viewModel,
            onDismiss = { showAddCityDialog = false },
            onCitySelected = { lat, lon, name ->
                viewModel.addCity(name, lat, lon)
                showAddCityDialog = false
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
fun AddCityDialog(
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
        title = { Text("Add City") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("City Name") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = locationName, style = MaterialTheme.typography.bodyLarge)
                        }
                        HorizontalDivider()
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
