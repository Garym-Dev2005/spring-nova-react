package com.example.api.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Size(max = 255)
    val username: String,
    // Capped so oversized input can't be used to burn CPU/memory in the hasher.
    @field:NotBlank @field:Size(max = 128)
    val password: String,
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
) {
    constructor(token: IssuedToken) : this(
        accessToken = token.value,
        expiresIn = token.expiresInSeconds,
    )
}

data class IssuedToken(
    val value: String,
    val expiresInSeconds: Long,
)
