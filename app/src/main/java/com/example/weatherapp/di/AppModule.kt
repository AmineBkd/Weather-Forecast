package com.example.weatherapp.di

import android.content.Context
import androidx.room.Room
import com.example.weatherapp.data.api.OpenWeatherApi
import com.example.weatherapp.data.db.AppDatabase
import com.example.weatherapp.data.db.CityDao
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import com.example.weatherapp.domain.repository.WeatherRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideOpenWeatherApi(retrofit: Retrofit): OpenWeatherApi =
        retrofit.create(OpenWeatherApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "weather_app_database"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideCityDao(database: AppDatabase): CityDao = database.cityDao()
}

/**
 * Binds the concrete [WeatherRepositoryImpl] to the domain-layer interface
 * [WeatherRepository]. This is the key Dependency Inversion step —
 * the domain layer depends only on the interface, never on the implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
}
