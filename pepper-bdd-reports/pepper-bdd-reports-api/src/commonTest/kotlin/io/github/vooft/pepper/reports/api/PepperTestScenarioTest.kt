package io.github.vooft.pepper.reports.api

import io.github.vooft.pepper.reports.api.PepperStepPrefix.GIVEN
import io.github.vooft.pepper.reports.api.PepperStepPrefix.THEN
import io.github.vooft.pepper.reports.api.PepperStepPrefix.WHEN
import io.github.vooft.pepper.reports.api.PepperTestStepDto.StepArgumentDto
import io.github.vooft.pepper.reports.api.PepperTestStepDto.StepErrorDto
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperTestScenarioTest : ShouldSpec({
    should("serialize PepperTestScenario") {
        val scenario = PepperTestScenarioDto(
            id = PepperTestScenarioDto.ScenarioId("scenario1"),
            version = 1,
            className = "io.github.pepper.reports.api.PepperTestProjectTest",
            tags = listOf(
                PepperTestScenarioDto.ScenarioTag("tag1"),
                PepperTestScenarioDto.ScenarioTag("tag2"),
            ),
            name = "serialize PepperTestProject",
            steps = listOf(
                PepperTestStepDto(
                    id = PepperTestStepDto.StepId("step1"),
                    index = 0,
                    prefix = GIVEN,
                    status = PepperTestStatus.PASSED,
                    name = "create PepperTestProject",
                    arguments = listOf(
                        StepArgumentDto(
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
                PepperTestStepDto(
                    id = PepperTestStepDto.StepId("step2"),
                    index = 1,
                    prefix = WHEN,
                    status = PepperTestStatus.PASSED,
                    name = "serialize PepperTestProject",
                    arguments = listOf(
                        StepArgumentDto(
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
                PepperTestStepDto(
                    id = PepperTestStepDto.StepId("step3"),
                    index = 2,
                    prefix = THEN,
                    status = PepperTestStatus.FAILED,
                    name = "assert PepperTestProject",
                    arguments = listOf(),
                    error = StepErrorDto(
                        message = "boohoo",
                        stacktrace = """
                            java.lang.AssertionError: boohoo
                                at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)
                        """.trimIndent()
                    ),
                    result = null,
                    startedAt = Instant.parse("2021-10-01T00:00:00Z"),
                    finishedAt = Instant.parse("2021-10-01T00:00:01Z"),
                ),
                PepperTestStepDto(
                    id = PepperTestStepDto.StepId("step4"),
                    index = 3,
                    prefix = THEN,
                    status = PepperTestStatus.SKIPPED,
                    name = "assert PepperTestProject again",
                    arguments = listOf(),
                    error = null,
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
            "id": "scenario1",
            "version": 1,
            "className": "io.github.pepper.reports.api.PepperTestProjectTest",
            "name": "serialize PepperTestProject",
            "tags": ["tag1", "tag2"],
            "steps": [
              {
                "id": "step1",
                "index": 0,
                "prefix": "GIVEN",
                "status": "PASSED",
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
                "index": 1,
                "prefix": "WHEN",
                "status": "PASSED",
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
                "index": 2,
                "prefix": "THEN",
                "status": "FAILED",
                "name": "assert PepperTestProject",
                "arguments": [],
                "error": {
                  "message": "boohoo",
                  "stacktrace": "java.lang.AssertionError: boohoo\n    at io.github.pepper.reports.api.PepperTestProjectTest.serialize PepperTestProject(PepperTestProjectTest.kt:42)"
                },
                "result": null,
                "startedAt": "2021-10-01T00:00:00Z",
                "finishedAt": "2021-10-01T00:00:01Z"
              },
              {
                "id": "step4",
                "index": 3,
                "prefix": "THEN",
                "status": "SKIPPED",
                "name": "assert PepperTestProject again",
                "arguments": [],
                "error": null,
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
