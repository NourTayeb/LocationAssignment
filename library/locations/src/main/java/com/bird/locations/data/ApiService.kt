package com.bird.locations.data

import com.bird.locations.data.model.request.LocationUpdateRequest
import com.bird.locations.data.model.response.AuthResponse
import com.bird.locations.data.model.response.LocationUpdateResponse
import com.bird.locations.data.model.response.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


internal interface ApiService {
    @POST("/auth")
    suspend fun getNewTokens(@Header("Authorization") bearerToken: String): Response<AuthResponse>

    @POST("/auth/refresh")
    suspend fun refreshAccessToken(@Header("Authorization") refreshToken: String): Response<RefreshTokenResponse>

    @POST("/location")
    suspend fun updateLocation(
        @Header("Authorization") accessToken: String,
        @Body location: LocationUpdateRequest
    ): Response<LocationUpdateResponse>
}