package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.di.AppContainer

class WeatherApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
