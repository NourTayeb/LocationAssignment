package com.bird.locations.data.model

import com.bird.locations.data.model.response.LocationUpdateResponse

sealed class UpdateLocationResult {
    data class Success(val locationUpdateResponse: LocationUpdateResponse) : UpdateLocationResult()
    data class UpdateLocationError(val e: Throwable?) : UpdateLocationResult()
    data class AuthenticationError(val e: Throwable?) : UpdateLocationResult()
    data class LocationRetrieveError(val e: Throwable?) : UpdateLocationResult()
    data class Error(val e: Throwable?) : UpdateLocationResult()
}