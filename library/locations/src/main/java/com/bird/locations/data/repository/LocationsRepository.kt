package com.bird.locations.data.repository

import com.bird.locations.data.model.UpdateLocationResult
import kotlinx.coroutines.flow.Flow

internal interface LocationsRepository {
    fun updateLocation(longitude: Double, latitude: Double): Flow<UpdateLocationResult>
}
