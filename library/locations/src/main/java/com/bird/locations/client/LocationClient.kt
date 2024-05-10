package com.bird.locations.client

import android.location.Location
import kotlinx.coroutines.flow.Flow

internal interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    suspend fun getLocation(): Location

    data class LocationException(val msg: String): Exception(msg)
}