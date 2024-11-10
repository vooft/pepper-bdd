package io.github.vooft.pepper.sample

import io.github.vooft.pepper.reports.PepperBddExtension
import io.kotest.core.config.AbstractProjectConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SampleProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(PepperBddExtension { println(Json.encodeToString(it)) })
}
