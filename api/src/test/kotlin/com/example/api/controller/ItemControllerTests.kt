package com.example.api.controller

import com.example.api.support.IntegrationTest
import com.example.api.support.TestUsers
import com.example.api.support.TestUsers.Companion.AUTHORIZATION
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.net.URI
import java.util.UUID

private const val UUID_V7 = "^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"

// Data is committed and left in the tmpfs `db-test` database for debugging;
// it is truncated once at the start of the next run (see TestDatabaseReset).
// Tests must not assume the table is empty or clean between tests.
@IntegrationTest
class ItemControllerTests(
    @Autowired private val mvc: MockMvc,
    @Autowired private val users: TestUsers,
) {
    @Test
    fun `creates and fetches an item`() {
        val bearer = users.bearer(mvc)
        mvc
            .post("/api/items") {
                header(AUTHORIZATION, bearer)
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"widget"}"""
            }.andExpect {
                status { isCreated() }
                jsonPath("$.name") { value("widget") }
                // UUIDv7: version nibble 7, RFC 9562 variant.
                jsonPath("$.id") { value(matchesPattern(UUID_V7)) }
            }

        mvc
            .get("/api/items") { header(AUTHORIZATION, bearer) }
            .andExpect {
                status { isOk() }
                jsonPath("$._embedded.items[*].name") { value(hasItem("widget")) }
                jsonPath("$._links.self.href") { exists() }
            }
    }

    @Test
    fun `items carry self and collection links`() {
        val bearer = users.bearer(mvc)
        val id = createItem(bearer, "linked")
        mvc
            .get("/api/items/$id") { header(AUTHORIZATION, bearer) }
            .andExpect {
                status { isOk() }
                jsonPath("$._links.self.href") { value(endsWith("/api/items/$id")) }
                jsonPath("$._links.collection.href") { value(endsWith("/api/items")) }
            }
    }

    @Test
    fun `walks forward by next links and back again by prev links`() {
        val bearer = users.bearer(mvc)
        val created = (1..5).map { createItem(bearer, "cursor-${UUID.randomUUID()}") }

        val forward = mutableListOf<List<String>>()
        var body = getPage("/api/items?size=2", bearer)
        assertNull(link(body, "prev"), "the first page has no prev link")
        while (true) {
            forward += ids(body)
            body = getPage(link(body, "next") ?: break, bearer)
        }
        assertTrue(forward.all { it.size <= 2 })
        val seen = forward.flatten()
        assertEquals(seen.distinct(), seen, "no item may appear on two pages")
        assertEquals(seen.sorted(), seen, "items are returned in id order")
        assertTrue(seen.containsAll(created))

        // Now walk back from the last page; each page must match the one we saw going forward.
        val backward = mutableListOf(ids(body))
        while (true) {
            body = getPage(link(body, "prev") ?: break, bearer)
            backward += ids(body)
        }
        assertEquals(forward.reversed(), backward)
    }

    @Test
    fun `rejects giving both cursor and before`() {
        mvc
            .get(
                "/api/items?cursor=${UUID.randomUUID()}&before=${UUID.randomUUID()}",
            ) { header(AUTHORIZATION, users.bearer(mvc)) }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `rejects an invalid page size or cursor`() {
        val bearer = users.bearer(mvc)
        listOf("size=0", "size=101", "cursor=not-a-uuid").forEach {
            mvc
                .get("/api/items?$it") { header(AUTHORIZATION, bearer) }
                .andExpect { status { isBadRequest() } }
        }
    }

    private fun getPage(
        href: String,
        bearer: String,
    ): String =
        mvc
            .get(href) { header(AUTHORIZATION, bearer) }
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

    private fun ids(body: String): List<String> = JsonPath.read(body, "$._embedded.items[*].id")

    /** The path and query of the named link, or null when the page has no such link. */
    private fun link(
        body: String,
        rel: String,
    ): String? =
        runCatching { JsonPath.read<String>(body, "$._links.$rel.href") }
            .getOrNull()
            ?.let { URI(it) }
            ?.let { "${it.rawPath}?${it.rawQuery}" }

    private fun createItem(
        bearer: String,
        name: String,
    ): String {
        val body =
            mvc
                .post("/api/items") {
                    header(AUTHORIZATION, bearer)
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"$name"}"""
                }.andExpect { status { isCreated() } }
                .andReturn()
                .response.contentAsString
        return JsonPath.read(body, "$.id")
    }

    @Test
    fun `rejects a blank name`() {
        mvc
            .post("/api/items") {
                header(AUTHORIZATION, users.bearer(mvc))
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":""}"""
            }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 404 for a missing item`() {
        mvc
            .get("/api/items/${UUID.randomUUID()}") { header(AUTHORIZATION, users.bearer(mvc)) }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `every item endpoint requires authentication`() {
        mvc.get("/api/items").andExpect { status { isUnauthorized() } }
        mvc.get("/api/items/1").andExpect { status { isUnauthorized() } }
        mvc
            .post("/api/items") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"nope"}"""
            }.andExpect { status { isUnauthorized() } }
        mvc.delete("/api/items/1").andExpect { status { isUnauthorized() } }
    }
}
