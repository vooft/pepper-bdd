package io.github.pepper.reports.kotest

import io.kotest.core.config.AbstractProjectConfig

object PepperBddProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(PepperProjectExtension())
}
