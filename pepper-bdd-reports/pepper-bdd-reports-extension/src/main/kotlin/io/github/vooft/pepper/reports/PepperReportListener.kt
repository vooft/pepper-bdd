package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperTestScenario
import io.github.vooft.pepper.reports.api.PepperTestSuite

interface PepperReportListener {
    suspend fun onScenarioFinished(scenario: PepperTestScenario)
    suspend fun onSuiteFinished(suite: PepperTestSuite)
}
