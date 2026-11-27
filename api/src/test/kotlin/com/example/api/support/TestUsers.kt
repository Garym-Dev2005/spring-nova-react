package com.example.api.support

import com.example.api.model.entity.AppUser
import com.example.api.repository.AppUserRepository
import com.jayway.jsonpath.JsonPath
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

/** Test helper: get-or-create a user (data is committed, so tests can't assume a clean table) and log in. */
@Component
class TestUsers(
    private val users: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun ensure(
        username: String,
        password: String,
    ): AppUser =
        users.findByUsernameIgnoreCase(username)
            ?: users.save(AppUser(username = username, passwordHash = passwordEncoder.encode(password)!!))

    /** Returns an `Authorization` header value for [username], creating the user if needed. */
    fun bearer(
        mvc: MockMvc,
        username: String = DEFAULT_USER,
        password: String = DEFAULT_PASSWORD,
    ): String {
        ensure(username, password)
        val body =
            mvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"username":"$username","password":"$password"}"""
                }.andReturn()
                .response.contentAsString
        return "Bearer ${JsonPath.read<String>(body, "$.accessToken")}"
    }

    companion object {
        const val DEFAULT_USER = "tester"
        const val DEFAULT_PASSWORD = "correct horse battery staple"
        const val AUTHORIZATION = HttpHeaders.AUTHORIZATION
    }
}
