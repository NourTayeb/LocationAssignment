package com.bird.locations.data.repository.util

import com.bird.locations.data.model.response.ErrorResponse
import com.google.gson.Gson
import retrofit2.Response

internal fun <T> Response<T>.getErrorExceptionFromResponse(): Exception {
    return UpdateLocationException(
        Gson().fromJson(
            errorBody()?.charStream(),
            ErrorResponse::class.java
        ).error
    )
}
internal const val AUTH_BEARER = "Bearer"
internal const val FORBIDDEN_REQUEST_CODE = 403

class UpdateLocationException(message: String) : Exception(message)

class AuthenticationException(message: String) : Exception(message)
