package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
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
            val homeViewModel: HomeViewModel = hiltViewModel()
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
            
            val detailViewModel: DetailViewModel = hiltViewModel()
            
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