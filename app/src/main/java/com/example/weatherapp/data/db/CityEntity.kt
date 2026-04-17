package com.example.weatherapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean = false
)
