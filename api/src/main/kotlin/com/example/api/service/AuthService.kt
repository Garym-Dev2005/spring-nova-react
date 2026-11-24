package com.example.api.service

import com.example.api.exception.InvalidCredentialsException
import com.example.api.model.dto.LoginRequest
import com.example.api.model.dto.TokenResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwt: JwtService,
) {
    fun login(request: LoginRequest): TokenResponse {
        val authentication =
            try {
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username, request.password),
                )
            } catch (_: AuthenticationException) {
                // One generic message for unknown user, bad password and disabled account.
                throw InvalidCredentialsException()
            }

        return TokenResponse(jwt.issue(authentication.name))
    }
}
