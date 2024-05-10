package com.bird.locations.di

import android.content.Context
import com.bird.locations.BuildConfig
import com.bird.locations.LocationUpdateUseCase
import com.bird.locations.LocationUpdateUseCaseImp
import com.bird.locations.api.NullAppContextException
import com.bird.locations.client.DefaultLocationClient
import com.bird.locations.data.ApiService
import com.bird.locations.data.repository.AuthenticationManager
import com.bird.locations.data.repository.LocationsRepository
import com.bird.locations.data.repository.LocationsRepositoryImp
import com.google.android.gms.location.LocationServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class ServiceLocator {

    companion object {

        var applicationContext: Context? = null

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val apiService: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }

        val authManager: AuthenticationManager by lazy {
            AuthenticationManager(
                BuildConfig.API_KEY,
                apiService
            )
        }

        val locationsRepository: LocationsRepository by lazy {
            LocationsRepositoryImp(apiService, authManager)
        }

        val locationClient by lazy {
            if (applicationContext == null) throw NullAppContextException()

            DefaultLocationClient(
                applicationContext!!,
                LocationServices.getFusedLocationProviderClient(applicationContext!!)
            )
        }

        val locationUpdateUseCase: LocationUpdateUseCase by lazy {
            LocationUpdateUseCaseImp(applicationContext, locationClient, locationsRepository)
        }

        fun provideApplicationContext(context: Context) {
            applicationContext = context
        }

    }
}