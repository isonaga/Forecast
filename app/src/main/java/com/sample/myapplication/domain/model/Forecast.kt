package com.sample.myapplication.domain.model

import com.google.gson.annotations.SerializedName

data class Forecast(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<ForecastItem>,
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain?,
    val sys: Sys,
    @SerializedName("dt_txt")
    val dtTxt: String,
)

data class Main(
    val temp: Double,

    @SerializedName("feels_like")
    val feelsLike: Double,

    @SerializedName("temp_min")
    val tempMin: Double,

    @SerializedName("temp_max")
    val tempMax: Double,

    val pressure: Int,

    @SerializedName("sea_level")
    val seaLevel: Int,

    @SerializedName("grnd_level")
    val grndLevel: Int,

    val humidity: Int,

    @SerializedName("temp_kf")
    val tempKf: Double,
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

data class Clouds(
    val all: Int,
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double?,
)

data class Rain(
    @SerializedName("3h")
    val h3: Double,
)

data class Sys(
    val pod: String,
)
