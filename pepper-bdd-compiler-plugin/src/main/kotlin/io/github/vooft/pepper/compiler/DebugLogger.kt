package io.github.vooft.pepper.compiler

import org.jetbrains.kotlin.cli.reportLog
import org.jetbrains.kotlin.config.CompilerConfiguration

internal data class DebugLogger(val debug: Boolean, val configuration: CompilerConfiguration) {
    fun log(message: String) {
        configuration.reportLog(message)
    }
}
