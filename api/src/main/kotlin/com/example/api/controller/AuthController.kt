package com.example.api.controller

import com.example.api.model.dto.LoginRequest
import com.example.api.model.dto.TokenResponse
import com.example.api.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
class AuthController(
    private val auth: AuthService,
) {
    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Exchange a username and password for a JWT access token")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): TokenResponse = auth.login(request)
}
