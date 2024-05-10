package com.bird.locations.data.repository

import com.bird.locations.data.ApiService
import com.bird.locations.data.model.response.AuthResponse
import com.bird.locations.data.model.response.RefreshTokenResponse
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthenticationManagerTest {

    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        authenticationManager = AuthenticationManager("apiKey", apiService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test successful authentication`() = runTest {
        val authResponse = AuthResponse("accessToken", "3024-05-09T13:02:26.589Z","refresh_token")
        val response = Response.success(authResponse)
        coEvery { apiService.getNewTokens(any()) } returns response

        authenticationManager.makeAuthenticatedApiCall { false }

        coVerify { apiService.getNewTokens(any()) }
    }

    @Test
    fun `makeAuthenticatedApiCall will call refreshAccessToken when lambda returns true`() =
        runTest {
            val accessToken = "refresh_token"
            val authResponse = AuthResponse("accessToken", "3024-05-09T13:02:26.589Z", accessToken)
            val refreshedAuthResponse =
                RefreshTokenResponse("accessToken", "3024-05-09T13:02:26.589Z")
            val response = Response.success(authResponse)
            val refreshedResponse = Response.success(refreshedAuthResponse)
            coEvery { apiService.getNewTokens(any()) } returns response
            coEvery { apiService.refreshAccessToken(any()) } returns refreshedResponse

            authenticationManager.makeAuthenticatedApiCall { true }
            coVerify { apiService.refreshAccessToken("Bearer $accessToken") }
        }

    @Test
    fun `makeAuthenticatedApiCall will call refreshAccessToken when lambda returns false`() =
        runTest {
            val accessToken = "refresh_token"
            val authResponse = AuthResponse("accessToken", "3024-05-09T13:02:26.589Z", accessToken)
            val refreshedAuthResponse =
                RefreshTokenResponse("accessToken", "3024-05-09T13:02:26.589Z")
            val response = Response.success(authResponse)
            val refreshedResponse = Response.success(refreshedAuthResponse)
            coEvery { apiService.getNewTokens(any()) } returns response
            coEvery { apiService.refreshAccessToken(any()) } returns refreshedResponse

            authenticationManager.makeAuthenticatedApiCall { false }
            coVerify(exactly = 0) { apiService.refreshAccessToken("Bearer $accessToken") }
        }

}