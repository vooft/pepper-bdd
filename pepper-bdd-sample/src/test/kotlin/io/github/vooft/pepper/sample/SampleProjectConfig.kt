package io.github.vooft.pepper.sample

import io.github.vooft.pepper.reports.kotest.PepperBddExtension
import io.kotest.core.config.AbstractProjectConfig

object SampleProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(PepperBddExtension())
}
