package api

import io.github.pepper.reports.api.PepperScenarioStatus.PASSED
import io.github.pepper.reports.api.PepperTestProject
import io.github.pepper.reports.api.PepperTestScenario
import io.github.pepper.reports.api.PepperTestStep
import io.github.pepper.reports.api.PepperTestStep.StepArgument
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperTestProjectTest :
    ShouldSpec({
        should("serialize PepperTestProject") {
            val project = PepperTestProject(
                version = 1,
                scenarios = listOf(
                    PepperTestScenario(
                        className = "io.github.pepper.reports.api.PepperTestProjectTest",
                        name = "serialize PepperTestProject",
                        steps = listOf(
                            PepperTestStep(
                                name = "create PepperTestProject",
                                arguments = listOf(
                                    StepArgument(
                                        name = "number",
                                        type = "kotlin.Int",
                                        value = "1"
                                    )
                                ),
                                error = null,
                                startedAt = Instant.parse("2021-08-01T00:00:00Z"),
                                finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
                            ),
                            PepperTestStep(
                                name = "serialize PepperTestProject",
                                arguments = listOf(
                                    StepArgument(
                                        name = "project",
                                        type = "io.github.pepper.reports.api.PepperTestProject",
                                        value = "fancy to string implementation"
                                    )
                                ),
                                error = null,
                                startedAt = Instant.parse("2021-09-01T00:00:00Z"),
                                finishedAt = Instant.parse("2021-09-01T00:00:01Z"),
                            ),
                            PepperTestStep(
                                name = "assert PepperTestProject",
                                arguments = listOf(),
                                error = """
                                java.lang.AssertionError: boohoo
                                    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)
                                """.trimIndent(),
                                startedAt = Instant.parse("2021-10-01T00:00:00Z"),
                                finishedAt = Instant.parse("2021-10-01T00:00:01Z"),
                            )
                        ),
                        status = PASSED,
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
              "version": 1,
              "scenarios": [
                {
                  "className": "io.github.pepper.reports.api.PepperTestProjectTest",
                  "name": "serialize PepperTestProject",
                  "steps": [
                    {
                      "name": "create PepperTestProject",
                      "arguments": [
                        {
                          "name": "number",
                          "type": "kotlin.Int",
                          "value": "1"
                        }
                      ],
                      "error": null,
                      "startedAt": "2021-08-01T00:00:00Z",
                      "finishedAt": "2021-08-01T00:00:01Z"
                    },
                    {
                      "name": "serialize PepperTestProject",
                      "arguments": [
                        {
                          "name": "project",
                          "type": "io.github.pepper.reports.api.PepperTestProject",
                          "value": "fancy to string implementation"
                        }
                      ],
                      "error": null,
                      "startedAt": "2021-09-01T00:00:00Z",
                      "finishedAt": "2021-09-01T00:00:01Z"
                    },
                    {
                      "name": "assert PepperTestProject",
                      "arguments": [],
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
