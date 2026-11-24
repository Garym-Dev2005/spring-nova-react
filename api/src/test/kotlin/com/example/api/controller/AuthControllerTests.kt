package com.example.api.controller

import com.example.api.model.dto.LoginRequest
import com.example.api.model.entity.AppUser
import com.example.api.repository.AppUserRepository
import com.example.api.support.IntegrationTest
import com.example.api.support.TestUsers
import com.example.api.support.TestUsers.Companion.AUTHORIZATION
import com.example.api.support.TestUsers.Companion.DEFAULT_PASSWORD
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.databind.json.JsonMapper

@IntegrationTest
class AuthControllerTests(
    @Autowired private val mvc: MockMvc,
    @Autowired private val users: TestUsers,
    @Autowired private val userRepository: AppUserRepository,
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val json: JsonMapper,
) {
    private fun login(
        username: String,
        password: String,
    ) = mvc.post("/api/auth/login") {
        contentType = MediaType.APPLICATION_JSON
        content = json.writeValueAsString(LoginRequest(username, password))
    }

    @Test
    fun `login returns a bearer token that opens protected endpoints`() {
        users.ensure("alice", "s3cret-passphrase")

        val token =
            login("alice", "s3cret-passphrase")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.tokenType") { value("Bearer") }
                    jsonPath("$.expiresIn") { value(3600) }
                    jsonPath("$.accessToken") { exists() }
                }.andReturn()
                .response.contentAsString
                .let {
                    com.jayway.jsonpath.JsonPath
                        .read<String>(it, "$.accessToken")
                }

        mvc
            .get("/api/items") { header(AUTHORIZATION, "Bearer $token") }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `usernames are case-insensitive`() {
        users.ensure("Bob", "another-passphrase")
        login("bob", "another-passphrase").andExpect { status { isOk() } }
    }

    @Test
    fun `wrong password and unknown user both get the same 401`() {
        users.ensure("carol", "right-passphrase")
        val wrongPassword = login("carol", "wrong").andExpect { status { isUnauthorized() } }.andReturn()
        val unknownUser = login("nobody", "whatever").andExpect { status { isUnauthorized() } }.andReturn()
        assertThat(JsonPath.read<String>(wrongPassword.response.contentAsString, "$.detail"))
            .isEqualTo("Invalid username or password")
            .isEqualTo(JsonPath.read<String>(unknownUser.response.contentAsString, "$.detail"))
    }

    @Test
    fun `disabled users cannot log in`() {
        val user = users.ensure("dave", "dave-passphrase")
        user.enabled = false
        userRepository.save(user)
        login("dave", "dave-passphrase").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `a disabled username can be reused by a new active user`() {
        val old = users.ensure("erin", "old-passphrase")
        old.enabled = false
        userRepository.save(old)
        userRepository.save(AppUser(username = "Erin", passwordHash = passwordEncoder.encode("new-passphrase")!!))
        login("erin", "new-passphrase").andExpect { status { isOk() } }
        login("erin", "old-passphrase").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `two active users cannot share a username ignoring case`() {
        users.ensure("frank", "frank-passphrase")
        assertThrows<DataIntegrityViolationException> {
            userRepository.saveAndFlush(AppUser(username = "FRANK", passwordHash = "x"))
        }
    }

    @Test
    fun `blank credentials are rejected`() {
        login("", "").andExpect { status { isBadRequest() } }
    }

    @Test
    fun `garbage and missing tokens are rejected`() {
        mvc
            .get("/api/items") { header(AUTHORIZATION, "Bearer not.a.jwt") }
            .andExpect { status { isUnauthorized() } }
        mvc.get("/api/items").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `passwords are stored as argon2id hashes, never plaintext`() {
        users.ensure(TestUsers.DEFAULT_USER, DEFAULT_PASSWORD)
        val hash = userRepository.findByUsernameIgnoreCase(TestUsers.DEFAULT_USER)!!.passwordHash
        assertThat(hash).startsWith("{argon2}\$argon2id\$").doesNotContain(DEFAULT_PASSWORD)
    }

    @Test
    fun `health and openapi docs stay public`() {
        mvc.get("/actuator/health").andExpect { status { isOk() } }
        mvc.get("/v3/api-docs").andExpect {
            status { isOk() }
            jsonPath("$.components.securitySchemes.bearerAuth.scheme") { value("bearer") }
        }
        mvc.get("/api/items").andExpect { status { is4xxClientError() } }
    }
}
