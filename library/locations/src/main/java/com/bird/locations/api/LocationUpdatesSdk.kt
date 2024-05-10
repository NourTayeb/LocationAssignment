package com.bird.locations.api

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.bird.locations.LocationService
import com.bird.locations.LocationUpdateUseCase
import com.bird.locations.di.ServiceLocator
import com.bird.locations.util.hasLocationPermission

class LocationUpdatesSdk {

    companion object {

        /**
         * initialize the sdk.
         * @param context:[Context] is the application context.
         */
        fun init(context: Context) {
            ServiceLocator.provideApplicationContext(context)
        }

        /**
         * Starts a foreground service that tracks user location and updates Bird's servers with it.
         * In order for it to work, Location permission must be granted and GPS service should be enabled.
         * @param interval: represents the time between each location update.
         */
        fun startLocationUpdatesService(interval: Long) {
            validatePermissionsAndLocationGranted()
            if (interval !in 3000L..20000) throw IntervalOutOfRangeException()
            Intent(ServiceLocator.applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                putExtra(LocationService.INTERVAL_EXTRA, interval)
                ServiceLocator.applicationContext?.startService(this)
                    ?: throw NullAppContextException()
            }
        }

        /**
         * Stops the foreground location service that's requesting and updates user location.
         */
        fun stopLocationUpdatesService() {
            Intent(ServiceLocator.applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                ServiceLocator.applicationContext?.startService(this)
                    ?: throw NullAppContextException()
            }
        }

        /**
         * returns a [LocationUpdateUseCase] which has the function [LocationUpdateUseCase.sendUpdatedLocation]
         * that is used to do one time location update.
         */
        fun getLocationUpdateUseCase(): LocationUpdateUseCase {
            return ServiceLocator.locationUpdateUseCase
        }

        internal fun validatePermissionsAndLocationGranted() {
            if (!allPermissionGranted()) throw PermissionsNotGrantedException()
            if (!gpsEnabled()) throw GpsNotEnabledException()
        }

        private fun allPermissionGranted(): Boolean {
            return ServiceLocator.applicationContext?.hasLocationPermission() ?: false
        }

        private fun gpsEnabled(): Boolean {
            val locationManager =
                ServiceLocator.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            return isGpsEnabled || isNetworkEnabled
        }
    }
}

class NullAppContextException :
    IllegalStateException("ApplicationContext not provided, please LocationUpdatesSdk.init(context) first.")

class PermissionsNotGrantedException : IllegalStateException("Location permissions not granted.")

class GpsNotEnabledException : IllegalStateException("GPS service is not enabled.")

class IntervalOutOfRangeException :
    IllegalStateException("Interval is outside of allowed range which is 4000L .. 20000L")

