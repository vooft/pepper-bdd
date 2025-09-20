package io.kotest.provided

import io.github.vooft.pepper.reports.PepperBddExtension
import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(
        PepperBddExtension.Companion.writeToFiles("build/reports/pepper")
    )
}
