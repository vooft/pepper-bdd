package io.github.vooft.pepper.reports.api

import io.github.vooft.pepper.reports.api.PepperStepPrefix.GIVEN
import io.github.vooft.pepper.reports.api.PepperStepPrefix.THEN
import io.github.vooft.pepper.reports.api.PepperStepPrefix.WHEN
import io.github.vooft.pepper.reports.api.PepperTestStep.StepArgument
import io.github.vooft.pepper.reports.api.PepperTestStep.StepError
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperTestScenarioTest : ShouldSpec({
    should("serialize PepperTestScenario") {
        val scenario = PepperTestScenario(
            version = 1,
            id = "scenario1",
            className = "io.github.pepper.reports.api.PepperTestProjectTest",
            name = "serialize PepperTestProject",
            steps = listOf(
                PepperTestStep(
                    id = "step1",
                    prefix = GIVEN,
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
                    prefix = WHEN,
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
                    prefix = THEN,
                    name = "assert PepperTestProject",
                    arguments = listOf(),
                    error = StepError(
                        message = "boohoo",
                        stacktrace = """
                            java.lang.AssertionError: boohoo
                                at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)
                        """.trimIndent()
                    ),
                    result = null,
                    startedAt = Instant.parse("2021-10-01T00:00:00Z"),
                    finishedAt = Instant.parse("2021-10-01T00:00:01Z"),
                )
            ),
            startedAt = Instant.parse("2021-08-01T00:00:00Z"),
            finishedAt = Instant.parse("2021-08-01T00:00:01Z"),
        )

        val json = Json.encodeToString(scenario)

        // language=JSON
        json shouldEqualJson """
          {
            "version": 1,
            "id": "scenario1",
            "className": "io.github.pepper.reports.api.PepperTestProjectTest",
            "name": "serialize PepperTestProject",
            "steps": [
              {
                "id": "step1",
                "prefix": "GIVEN",
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
                "prefix": "WHEN",
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
                "prefix": "THEN",
                "name": "assert PepperTestProject",
                "arguments": [],
                "error": {
                  "message": "boohoo",
                  "stacktrace": "java.lang.AssertionError: boohoo\n    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)"
                },
                "result": null,
                "startedAt": "2021-10-01T00:00:00Z",
                "finishedAt": "2021-10-01T00:00:01Z"
              }
            ],
            "startedAt": "2021-08-01T00:00:00Z",
            "finishedAt": "2021-08-01T00:00:01Z"
          }
        """.trimIndent()
    }
})
