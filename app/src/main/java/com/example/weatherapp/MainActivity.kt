package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weatherapp.ui.detail.DetailScreen
import com.example.weatherapp.ui.detail.DetailViewModel
import com.example.weatherapp.ui.home.HomeScreen
import com.example.weatherapp.ui.home.HomeViewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherAppNavGraph()
                }
            }
        }
    }
}

@Composable
fun WeatherAppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            val appContainer = (androidx.compose.ui.platform.LocalContext.current.applicationContext as WeatherApplication).container
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(appContainer.weatherRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToDetail = { lat, lon, name ->
                    navController.navigate("detail/$lat/$lon/$name")
                }
            )
        }
        composable(
            route = "detail/{lat}/{lon}/{name}",
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            val name = backStackEntry.arguments?.getString("name") ?: ""
            
            val appContainer = (androidx.compose.ui.platform.LocalContext.current.applicationContext as WeatherApplication).container
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.provideFactory(appContainer.weatherRepository)
            )
            
            DetailScreen(
                cityName = name,
                lat = lat,
                lon = lon,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}