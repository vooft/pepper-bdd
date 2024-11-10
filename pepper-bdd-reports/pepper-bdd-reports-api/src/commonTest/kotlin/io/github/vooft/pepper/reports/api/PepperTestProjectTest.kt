package io.github.vooft.pepper.reports.api

import io.github.vooft.pepper.reports.api.PepperScenarioStatus.PASSED
import io.github.vooft.pepper.reports.api.PepperTestStep.StepArgument
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperTestProjectTest : ShouldSpec({
    should("serialize PepperTestProject") {
        val project = PepperProjectReport(
            version = 1,
            scenarios = listOf(
                PepperTestScenario(
                    id = "scenario1",
                    className = "io.github.pepper.reports.api.PepperTestProjectTest",
                    name = "serialize PepperTestProject",
                    steps = listOf(
                        PepperTestStep(
                            id = "step1",
                            name = "create PepperTestProject",
                            arguments = listOf(
                                StepArgument(
                                    name = "number",
                                    type = "kotlin.Int",
                                    value = "1"
                                )
                            ),
                            error = null,
                            result = "test",
                            startedAt = Instant.parse("2021-08-01T00:00:00Z"),
                            finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
                        ),
                        PepperTestStep(
                            id = "step2",
                            name = "serialize PepperTestProject",
                            arguments = listOf(
                                StepArgument(
                                    name = "project",
                                    type = "io.github.pepper.reports.api.PepperTestProject",
                                    value = "fancy to string implementation"
                                )
                            ),
                            error = null,
                            result = "[{}]",
                            startedAt = Instant.parse("2021-09-01T00:00:00Z"),
                            finishedAt = Instant.parse("2021-09-01T00:00:01Z"),
                        ),
                        PepperTestStep(
                            id = "step3",
                            name = "assert PepperTestProject",
                            arguments = listOf(),
                            error = """
                                java.lang.AssertionError: boohoo
                                    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)
                            """.trimIndent(),
                            result = null,
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
                  "id": "scenario1",
                  "className": "io.github.pepper.reports.api.PepperTestProjectTest",
                  "name": "serialize PepperTestProject",
                  "steps": [
                    {
                      "id": "step1",
                      "name": "create PepperTestProject",
                      "arguments": [
                        {
                          "name": "number",
                          "type": "kotlin.Int",
                          "value": "1"
                        }
                      ],
                      "error": null,
                      "result": "test",
                      "startedAt": "2021-08-01T00:00:00Z",
                      "finishedAt": "2021-08-01T00:00:01Z"
                    },
                    {
                      "id": "step2",
                      "name": "serialize PepperTestProject",
                      "arguments": [
                        {
                          "name": "project",
                          "type": "io.github.pepper.reports.api.PepperTestProject",
                          "value": "fancy to string implementation"
                        }
                      ],
                      "error": null,
                      "result": "[{}]",
                      "startedAt": "2021-09-01T00:00:00Z",
                      "finishedAt": "2021-09-01T00:00:01Z"
                    },
                    {
                      "id": "step3",
                      "name": "assert PepperTestProject",
                      "arguments": [],
                      "error": "java.lang.AssertionError: boohoo\n    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)",
                      "result": null,
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
