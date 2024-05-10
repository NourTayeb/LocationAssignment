package com.bird.locations.data.repository

import com.bird.locations.data.ApiService
import com.bird.locations.data.model.UpdateLocationResult
import com.bird.locations.data.model.response.LocationUpdateResponse
import com.bird.locations.data.repository.util.AuthenticationException
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class LocationsRepositoryImpTest {

    private lateinit var repository: LocationsRepositoryImp

    @RelaxedMockK
    private lateinit var apiService: ApiService

    @MockK
    private lateinit var authenticationManager: AuthenticationManager

    @Before
    fun init() {
        MockKAnnotations.init(this)
        repository = LocationsRepositoryImp(apiService, authenticationManager)
    }

    @Test
    fun `test updateLocation success`() = runTest {

        val callbackSlot: CapturingSlot<suspend (String) -> Boolean> = slot()
        coEvery { authenticationManager.makeAuthenticatedApiCall(capture(callbackSlot)) } coAnswers {
            val callback = callbackSlot.captured
            callback.invoke("mockedAccessToken")
        }

        val mockedLocationResponse = mockk<LocationUpdateResponse>()

        coEvery { apiService.updateLocation(any(), any()) } returns Response.success(
            200,
            mockedLocationResponse
        )

        val result: Flow<UpdateLocationResult> = repository.updateLocation(0.0, 0.0)

        result.collect { updateLocationResult ->
            assertEquals(UpdateLocationResult.Success(mockedLocationResponse), updateLocationResult)
        }

        coVerify { authenticationManager.makeAuthenticatedApiCall(any()) }
        coVerify { apiService.updateLocation(any(), any()) }
    }
    @Test
    fun `test updateLocation authentication error`() = runTest {
        coEvery { authenticationManager.makeAuthenticatedApiCall(any()) } throws AuthenticationException("error")

        val result: Flow<UpdateLocationResult> = repository.updateLocation(0.0, 0.0)

        result.collect { updateLocationResult ->
            assertTrue(updateLocationResult is UpdateLocationResult.AuthenticationError)
        }

        coVerify { authenticationManager.makeAuthenticatedApiCall(any()) }
        coVerify(exactly = 0) { apiService.updateLocation(any(), any()) }
    }

    @Test
    fun `test updateLocation generic error`() = runTest {
        val callbackSlot: CapturingSlot<suspend (String) -> Boolean> = slot()
        coEvery { authenticationManager.makeAuthenticatedApiCall(capture(callbackSlot)) } coAnswers {
            val callback = callbackSlot.captured
            callback.invoke("mockedAccessToken")
        }

        coEvery { apiService.updateLocation(any(), any()) } throws RuntimeException("Generic error")

        val result: Flow<UpdateLocationResult> = repository.updateLocation(0.0, 0.0)

        result.collect { updateLocationResult ->
            assertTrue(updateLocationResult is UpdateLocationResult.Error)
        }

        coVerify { authenticationManager.makeAuthenticatedApiCall(any()) }
        coVerify { apiService.updateLocation(any(), any()) }
    }
}
