package com.sample.myapplication.infra

import com.sample.myapplication.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastRepository @Inject constructor(
    private val forecastApi: ForecastApi,
) {
    suspend fun get5day(
        city: String,
    ) = forecastApi.get5day(
        city = city,
        appId = BuildConfig.API_KEY,
    )

    suspend fun get5dayLocation(
        latitude: Double,
        longitude: Double,
    ) = forecastApi.get5dayLocation(
        latitude = latitude,
        longitude = longitude,
        appId = BuildConfig.API_KEY,
    )
}
