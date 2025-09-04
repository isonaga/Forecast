package com.sample.myapplication.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sample.myapplication.ui.screens.ForecastScreen
import com.sample.myapplication.ui.screens.SelectRegionScreen
import com.sample.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private sealed class Route {
        @Serializable
        object SelectRegion : Route()

        @Serializable
        object Forecast : Route()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val uiState = viewModel.rememberMainUiState()

                NavHost(
                    navController = navController,
                    startDestination = Route.SelectRegion
                ) {
                    composable<Route.SelectRegion> {
                        SelectRegionScreen(
                            onSelectRegion = { region ->
                                uiState.updateRegion(region)
                                navController.navigate(Route.Forecast)
                            },
                        )
                    }
                    composable<Route.Forecast> {
                        ForecastScreen(
                            navController = navController,
                            uiState = uiState,
                        )
                    }
                }
            }
        }
    }
}
