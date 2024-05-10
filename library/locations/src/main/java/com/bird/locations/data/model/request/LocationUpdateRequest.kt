package com.bird.locations.data.model.request

internal data class LocationUpdateRequest (
    private val longitude: Double = 0.0,
    private val latitude: Double = 0.0
)