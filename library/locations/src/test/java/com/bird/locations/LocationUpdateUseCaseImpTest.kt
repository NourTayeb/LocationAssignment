package com.bird.locations

import android.content.Context
import com.bird.locations.api.NullAppContextException
import com.bird.locations.client.LocationClient
import com.bird.locations.data.model.UpdateLocationResult
import com.bird.locations.data.repository.LocationsRepository
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LocationUpdateUseCaseImpTest {

    private lateinit var locationClient: LocationClient
    private lateinit var locationsRepository: LocationsRepository
    private lateinit var context: Context
    private lateinit var useCase: LocationUpdateUseCaseImp

    @Before
    fun setup() {
        locationClient = mockk(relaxed = true)
        locationsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)
        useCase = LocationUpdateUseCaseImp(context, locationClient, locationsRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }


    @Test(expected = NullAppContextException::class)
    fun `sendUpdatedLocation when ApplicationContext is null`() {
        useCase = LocationUpdateUseCaseImp(null, locationClient, locationsRepository)
        runTest {
            val result = useCase.sendUpdatedLocation()
            result.toList()
        }
    }

    @Test
    fun `sendUpdatedLocation when LocationClient throws exception`() {
        coEvery { locationClient.getLocation() } throws LocationClient.LocationException("Location error")

        runTest {
            val result = useCase.sendUpdatedLocation().toList()
            Assert.assertEquals(1, result.size)
            Assert.assertEquals(
                UpdateLocationResult.LocationRetrieveError(
                    LocationClient.LocationException(
                        "Location error"
                    )
                ), result[0]
            )
        }
    }

    @Test
    fun `sendUpdatedLocation when LocationClient retrieves location successfully`() {
        coEvery { locationClient.getLocation() } returns mockk(relaxed = true) // Mocking a location

        coEvery { locationsRepository.updateLocation(any(), any()) } returns flow {
            emit(mockk<UpdateLocationResult.Success>())
        }

        runTest {
            val result = useCase.sendUpdatedLocation().toList()
            Assert.assertEquals(1, result.size)
            assert(result[0] is UpdateLocationResult.Success)
        }
    }
}
