package io.github.vooft.pepper.sample

import io.github.vooft.pepper.reports.PepperBddExtension
import io.github.vooft.pepper.reports.PepperReportListener
import io.github.vooft.pepper.reports.api.PepperTestScenario
import io.github.vooft.pepper.reports.api.PepperTestSuite
import io.kotest.core.config.AbstractProjectConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SampleProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(
        PepperBddExtension(object : PepperReportListener {
            override suspend fun onScenarioFinished(scenario: PepperTestScenario) {
                println(Json.encodeToString(scenario))
            }

            override suspend fun onSuiteFinished(suite: PepperTestSuite) {
                println(Json.encodeToString(suite))
            }
        })
    )
}
