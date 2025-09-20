package io.github.vooft.pepper.sample

import io.github.vooft.pepper.reports.PepperBddExtension
import io.kotest.core.config.AbstractProjectConfig

object SampleProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(
        PepperBddExtension.writeToFiles("build/reports/pepper")
    )
}
