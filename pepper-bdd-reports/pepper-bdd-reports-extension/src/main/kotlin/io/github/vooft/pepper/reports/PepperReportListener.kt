package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperTestScenarioDto
import io.github.vooft.pepper.reports.api.PepperTestSuiteDto

interface PepperReportListener {
    suspend fun onScenarioFinished(scenario: PepperTestScenarioDto)
    suspend fun onSuiteFinished(suite: PepperTestSuiteDto)
}
