package com.sample.myapplication.infra

import com.sample.myapplication.domain.model.Forecast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {
    @GET("data/2.5/forecast")
    suspend fun get5day(
        @Query("q") city: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ja",
        @Query("appId") appId: String,
    ): Response<Forecast>

    @GET("data/2.5/forecast")
    suspend fun get5dayLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ja",
        @Query("appId") appId: String,
    ): Response<Forecast>
}
