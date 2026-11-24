package com.example.api.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * Full-context test against the tmpfs `db-test` Postgres (`test` profile).
 * Tests commit their data; [TestDatabaseReset] truncates once at the start of each run.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseReset::class, TestUsers::class)
annotation class IntegrationTest
