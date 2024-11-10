package io.github.vooft.pepper.reports.api

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperTestSuiteTest : ShouldSpec({
    should("serialize PepperTestSuite") {
        val project = PepperTestSuite(
            version = 1,
            scenarios = listOf("1", "2"),
            startedAt = Instant.parse("2021-08-01T00:00:00Z"),
            finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
        )

        val json = Json.encodeToString(project)

        // language=JSON
        json shouldEqualJson """
            {
              "version": 1,
              "scenarios": ["1", "2"],
              "startedAt": "2021-08-01T00:00:00Z",
              "finishedAt": "2021-08-01T00:00:01Z"
            }
        """.trimIndent()
    }
})
