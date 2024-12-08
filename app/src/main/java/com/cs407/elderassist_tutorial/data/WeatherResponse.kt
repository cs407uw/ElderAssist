package com.cs407.elderassist_tutorial.data

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String
)

data class Main(val temp: Double, val humidity: Int)
data class Weather(val description: String, val icon: String)
