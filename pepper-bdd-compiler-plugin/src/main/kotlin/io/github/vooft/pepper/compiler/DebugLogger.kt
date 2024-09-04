package io.github.vooft.pepper.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal data class DebugLogger(val debug: Boolean, val messageCollector: MessageCollector) {
    fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.LOGGING, message)
    }
}
