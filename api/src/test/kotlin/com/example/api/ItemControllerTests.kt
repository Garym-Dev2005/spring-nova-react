package com.example.api

import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

// Data is committed and left in the tmpfs `db-test` database for debugging;
// it is truncated once at the start of the next run (see TestDatabaseReset).
// Tests must not assume the table is empty or clean between tests.
@IntegrationTest
class ItemControllerTests(
    @Autowired private val mvc: MockMvc,
) {
    @Test
    fun `creates and fetches an item`() {
        mvc
            .post("/api/items") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"widget"}"""
            }.andExpect {
                status { isCreated() }
                jsonPath("$.name") { value("widget") }
            }

        mvc.get("/api/items").andExpect {
            status { isOk() }
            jsonPath("$[*].name") { value(hasItem("widget")) }
        }
    }

    @Test
    fun `rejects a blank name`() {
        mvc
            .post("/api/items") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":""}"""
            }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 404 for a missing item`() {
        mvc.get("/api/items/999999").andExpect { status { isNotFound() } }
    }
}
