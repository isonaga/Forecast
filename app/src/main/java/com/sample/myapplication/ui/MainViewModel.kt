package com.sample.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sample.myapplication.domain.model.Forecast
import com.sample.myapplication.infra.ForecastRepository
import com.sample.myapplication.ui.screens.SelectRegionScreen
import com.sample.myapplication.ui.screens.SelectRegionScreen.Region
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val forecastRepository: ForecastRepository,
) : ViewModel() {

    @Composable
    fun rememberMainUiState(): MainUiState {
        val coroutineScope = rememberCoroutineScope()
        var status by remember { mutableStateOf(MainUiState.Status.READY) }
        var region by remember { mutableStateOf(SelectRegionScreen.regions.first()) }
        val forecast = remember { mutableStateMapOf<Region, Forecast?>() }

        return remember(status, region, forecast) {
            MainUiState(
                status = status,
                region = region,
                forecast = forecast,
                updateRegion = {
                    region = it
                },
                refresh = {
                    status = MainUiState.Status.LOADING
                    coroutineScope.launch {
                        try {
                            val response = when (region) {
                                is Region.Location -> forecastRepository.get5dayLocation(
                                    latitude = (region as Region.Location).latitude,
                                    longitude = (region as Region.Location).longitude,
                                )

                                else -> forecastRepository.get5day(
                                    city = region.cityName,
                                )
                            }

                            if (response.isSuccessful) {
                                forecast[region] = response.body()
                                status = MainUiState.Status.SUCCESS
                            } else {
                                status = MainUiState.Status.ERROR
                            }
                        } catch (_: Throwable) {
                            status = MainUiState.Status.ERROR
                        }
                    }
                },
            )
        }
    }
}

data class MainUiState(
    val status: Status,
    val region: Region,
    val forecast: Map<Region, Forecast?>,

    val updateRegion: (Region) -> Unit,
    val refresh: () -> Unit,
) {
    enum class Status {
        READY,
        LOADING,
        SUCCESS,
        ERROR,
    }

    val currentForecast: Forecast?
        get() = forecast[region]
}
