package io.github.pepper.reports.api

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperTestProjectTest :
    ShouldSpec({
        should("serialize PepperTestProject") {
            val project = PepperTestProject(
                scenarios = listOf(
                    PepperTestScenario(
                        className = "io.github.pepper.reports.api.PepperTestProjectTest",
                        name = "serialize PepperTestProject",
                        steps = listOf(
                            PepperTestStep(
                                name = "create PepperTestProject",
                                error = null,
                                startedAt = Instant.parse("2021-08-01T00:00:00Z"),
                                finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
                            ),
                            PepperTestStep(
                                name = "serialize PepperTestProject",
                                error = null,
                                startedAt = Instant.parse("2021-09-01T00:00:00Z"),
                                finishedAt = Instant.parse("2021-09-01T00:00:01Z"),
                            ),
                            PepperTestStep(
                                name = "assert PepperTestProject",
                                error = """
                                java.lang.AssertionError: boohoo
                                    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)
                                """.trimIndent(),
                                startedAt = Instant.parse("2021-10-01T00:00:00Z"),
                                finishedAt = Instant.parse("2021-10-01T00:00:01Z"),
                            )
                        ),
                        status = PepperScenarioStatus.PASSED,
                        startedAt = Instant.parse("2021-08-01T00:00:00Z"),
                        finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
                    )
                ),
                startedAt = Instant.parse("2021-08-01T00:00:00Z"),
                finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
            )

            val json = Json.encodeToString(project)

            // language=JSON
            json shouldEqualJson """
            {
              "scenarios": [
                {
                  "className": "io.github.pepper.reports.api.PepperTestProjectTest",
                  "name": "serialize PepperTestProject",
                  "steps": [
                    {
                      "name": "create PepperTestProject",
                      "error": null,
                      "startedAt": "2021-08-01T00:00:00Z",
                      "finishedAt": "2021-08-01T00:00:01Z"
                    },
                    {
                      "name": "serialize PepperTestProject",
                      "error": null,
                      "startedAt": "2021-09-01T00:00:00Z",
                      "finishedAt": "2021-09-01T00:00:01Z"
                    },
                    {
                      "name": "assert PepperTestProject",
                      "error": "java.lang.AssertionError: boohoo\n    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)",
                      "startedAt": "2021-10-01T00:00:00Z",
                      "finishedAt": "2021-10-01T00:00:01Z"
                    }
                  ],
                  "status": "PASSED",
                  "startedAt": "2021-08-01T00:00:00Z",
                  "finishedAt": "2021-08-01T00:00:01Z"
                }
              ],
              "startedAt": "2021-08-01T00:00:00Z",
              "finishedAt": "2021-08-01T00:00:01Z"
            }
            """.trimIndent()
        }
    })
