package com.bird.locations

import android.content.Context
import com.bird.locations.api.LocationUpdatesSdk
import com.bird.locations.api.NullAppContextException
import com.bird.locations.client.LocationClient
import com.bird.locations.data.model.UpdateLocationResult
import com.bird.locations.data.repository.LocationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface LocationUpdateUseCase {
    fun sendUpdatedLocation(): Flow<UpdateLocationResult>
}

internal class LocationUpdateUseCaseImp(
    private val context: Context?,
    private val locationClient: LocationClient,
    private val locationsRepository: LocationsRepository
) : LocationUpdateUseCase {

    override fun sendUpdatedLocation(): Flow<UpdateLocationResult> = flow {
        LocationUpdatesSdk.validatePermissionsAndLocationGranted()
        if (context == null) throw NullAppContextException()
        try {
            val currentLocation = locationClient.getLocation()
            locationsRepository.updateLocation(currentLocation.longitude, currentLocation.latitude)
                .collect {
                    emit(it)
                }
        } catch (e: LocationClient.LocationException) {
            emit(UpdateLocationResult.LocationRetrieveError(e))
        }
    }
}