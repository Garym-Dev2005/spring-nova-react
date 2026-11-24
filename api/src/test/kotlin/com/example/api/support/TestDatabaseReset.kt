package com.example.api.support

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Truncates every table (except Liquibase's bookkeeping tables) once per test run, after
 * Liquibase has migrated. Nothing is cleaned up at the end of a test, so the data
 * can be inspected in `db-test` afterwards.
 */
@Component
class TestDatabaseReset(
    private val jdbc: JdbcTemplate,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!done.compareAndSet(false, true)) return
        val tables =
            jdbc.queryForList(
                """
                SELECT quote_ident(tablename) FROM pg_tables
                WHERE schemaname = current_schema() AND tablename NOT IN ('databasechangelog', 'databasechangeloglock')
                """.trimIndent(),
                String::class.java,
            )
        if (tables.isNotEmpty()) {
            jdbc.execute("TRUNCATE TABLE ${tables.joinToString()} RESTART IDENTITY CASCADE")
        }
    }

    private companion object {
        // One truncate per JVM (test run), even if several Spring contexts start.
        val done = AtomicBoolean(false)
    }
}
