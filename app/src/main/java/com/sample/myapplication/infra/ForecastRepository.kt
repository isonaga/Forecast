package com.sample.myapplication.infra

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sample.myapplication.BuildConfig
import com.sample.myapplication.domain.model.Forecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastRepository @Inject constructor(
    private val context: Context,
    private val forecastApi: ForecastApi,
) {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "cache",
    )

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

    suspend fun writeForecast(cityName: String, forecast: Forecast) {
        val jsonString = Json.encodeToString(Forecast.serializer(), forecast)
        writeValue(stringPreferencesKey(cityName), jsonString)
    }

    suspend fun readForecast(cityName: String): Forecast? {
        val jsonString = readValue(stringPreferencesKey(cityName), "").first()
        return if (jsonString.isNotEmpty()) {
            Json.decodeFromString(Forecast.serializer(), jsonString)
        } else {
            null
        }
    }

    private suspend fun <T> writeValue(key: Preferences.Key<T>, value: T) =
        runCatching {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        }.isSuccess

    private fun <T> readValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }
}
