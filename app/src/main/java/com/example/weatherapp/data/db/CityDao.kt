package com.example.weatherapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM city_table ORDER BY isCurrentLocation DESC, id ASC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: CityEntity)

    @Delete
    fun deleteCity(city: CityEntity)

    @Query("DELETE FROM city_table WHERE isCurrentLocation = 1")
    fun deleteCurrentLocationCity()

    @Query("SELECT COUNT(*) FROM city_table")
    fun getCitiesCount(): Int
}
