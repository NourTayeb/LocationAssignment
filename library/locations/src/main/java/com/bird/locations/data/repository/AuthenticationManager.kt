package com.bird.locations.data.repository

import com.bird.locations.data.ApiService
import com.bird.locations.data.model.response.AuthResponse
import com.bird.locations.data.model.response.RefreshTokenResponse
import com.bird.locations.data.repository.util.AUTH_BEARER
import com.bird.locations.data.repository.util.AuthenticationException
import com.bird.locations.data.repository.util.getErrorExceptionFromResponse

internal class AuthenticationManager(
    private val apiKey: String,
    private val apiService: ApiService
) {

    // Better be delegated to [DataStore] to store refreshToken instead of loading it every time app is opened.
    private var authResponse: AuthResponse? = null

    private suspend fun authenticate(): Result<AuthResponse> {
        return try {
            authResponse?.let { return Result.success(it) }
            val response = apiService.getNewTokens("$AUTH_BEARER $apiKey")
            if (response.isSuccessful && response.body() != null) {
                authResponse = response.body()
                Result.success(response.body()!!)
            } else {
                Result.failure(response.getErrorExceptionFromResponse())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun refreshAccessToken(refreshToken: String): Result<RefreshTokenResponse> {
        return try {
            val response = apiService.refreshAccessToken(refreshToken)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(response.getErrorExceptionFromResponse())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(AuthenticationException::class)
    suspend fun makeAuthenticatedApiCall(
        apiCall: suspend (String) -> Boolean
    ) {
        val authenticationResult = authenticate()
        when {
            authenticationResult.isSuccess && authenticationResult.getOrNull() != null -> {
                val authResponse = authenticationResult.getOrNull()!!
                when {
                    authResponse.accessTokenExpired -> {
                        val refreshTokenResult =
                            refreshAccessToken("$AUTH_BEARER ${authResponse.refreshToken}")
                        val newToken = refreshTokenResult.handleRefreshTokenResult()
                        this.authResponse?.accessToken = newToken
                        apiCall(authResponse.accessToken)
                    }

                    !authResponse.accessTokenExpired -> {
                        val shouldRefresh = apiCall(authResponse.accessToken)
                        // API returned should refresh even though local authResponse.accessTokenExpired says otherwise.
                        if (shouldRefresh) {
                            val refreshTokenResult =
                                refreshAccessToken("$AUTH_BEARER ${authResponse.refreshToken}")
                            val newToken = refreshTokenResult.handleRefreshTokenResult()
                            this.authResponse?.accessToken = newToken
                            apiCall(authResponse.accessToken)
                        }
                    }
                }
            }

            else -> {
                throw AuthenticationException("Failed to authenticate.")
            }
        }
    }

    private fun Result<RefreshTokenResponse>.handleRefreshTokenResult(): String {
        if (isSuccess && getOrNull() != null) {
            val refreshTokenResponse = getOrNull()!!
            return refreshTokenResponse.accessToken
        } else {
            throw AuthenticationException("Failed to refresh token.")
        }
    }

}