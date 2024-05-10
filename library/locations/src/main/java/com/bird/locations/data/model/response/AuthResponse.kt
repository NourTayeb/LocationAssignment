package com.bird.locations.data.model.response

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


internal data class AuthResponse(
    var accessToken: String,
    val expiresAt: String,
    val refreshToken: String
) {
    val accessTokenExpired: Boolean
        get() {
            val dateFormat = SimpleDateFormat(expiryDateFormat, Locale("en"))
            dateFormat.timeZone = TimeZone.getTimeZone(expiryDateTimezone)
            val expirationTimeUtc = dateFormat.parse(expiresAt)
            val currentUtcTime = Date()
            return currentUtcTime.after(expirationTimeUtc)
        }

    companion object {
        private const val expiryDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private const val expiryDateTimezone = "UTC"
    }
}