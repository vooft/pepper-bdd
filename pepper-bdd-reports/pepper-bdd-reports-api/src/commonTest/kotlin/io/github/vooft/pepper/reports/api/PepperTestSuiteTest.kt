package io.github.vooft.pepper.reports.api

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PepperTestSuiteTest : ShouldSpec({
    should("serialize PepperTestSuite") {
        val project = PepperTestSuiteDto(
            id = PepperTestSuiteDto.SuiteId("1"),
            version = 1,
            scenarios = listOf(
                PepperTestSuiteDto.ScenarioSummaryDto(PepperTestScenarioDto.ScenarioId("1"), "scenario1", PepperTestStatus.PASSED),
                PepperTestSuiteDto.ScenarioSummaryDto(PepperTestScenarioDto.ScenarioId("2"), "scenario2", PepperTestStatus.FAILED),
            ),
            startedAt = Instant.parse("2021-08-01T00:00:00Z"),
            finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
        )

        val json = Json.encodeToString(project)

        // language=JSON
        json shouldEqualJson """
            {
              "id": "1",
              "version": 1,
              "scenarios": [
                {
                  "id": "1",
                  "name": "scenario1",
                  "status": "PASSED"
                },
                {
                  "id": "2",
                  "name": "scenario2",
                  "status": "FAILED"
                }
              ],
              "startedAt": "2021-08-01T00:00:00Z",
              "finishedAt": "2021-08-01T00:00:01Z"
            }
        """.trimIndent()
    }
})
