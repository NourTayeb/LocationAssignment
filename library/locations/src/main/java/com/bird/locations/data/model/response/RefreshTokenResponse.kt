package com.bird.locations.data.model.response

internal data class RefreshTokenResponse(
    val accessToken: String,
    val expiresAt: String
)