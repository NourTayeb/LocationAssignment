package com.bird.locations.data.repository

import com.bird.locations.data.ApiService
import com.bird.locations.data.model.request.LocationUpdateRequest
import com.bird.locations.data.model.UpdateLocationResult
import com.bird.locations.data.repository.util.AUTH_BEARER
import com.bird.locations.data.repository.util.AuthenticationException
import com.bird.locations.data.repository.util.FORBIDDEN_REQUEST_CODE
import com.bird.locations.data.repository.util.getErrorExceptionFromResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class LocationsRepositoryImp(
    private val apiService: ApiService,
    private val authManager: AuthenticationManager
) : LocationsRepository {

    override fun updateLocation(
        longitude: Double,
        latitude: Double
    ): Flow<UpdateLocationResult> = flow {
        try {
            authManager.makeAuthenticatedApiCall { accessToken ->
                val response = apiService.updateLocation(
                    "$AUTH_BEARER $accessToken",
                    LocationUpdateRequest(longitude, latitude)
                )
                if (response.isSuccessful && response.body() != null) {
                    emit(UpdateLocationResult.Success(response.body()!!))
                } else {
                    emit(UpdateLocationResult.UpdateLocationError(response.getErrorExceptionFromResponse()))
                }
                return@makeAuthenticatedApiCall response.code() == FORBIDDEN_REQUEST_CODE
            }
        } catch (e: AuthenticationException) {
            emit(UpdateLocationResult.AuthenticationError(e))
        } catch (e: Exception) {
            emit(UpdateLocationResult.Error(e))
        }
    }

}

