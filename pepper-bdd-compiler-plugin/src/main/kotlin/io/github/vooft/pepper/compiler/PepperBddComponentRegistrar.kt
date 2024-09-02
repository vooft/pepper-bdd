package io.github.vooft.pepper.compiler

import io.github.vooft.pepper.compiler.transform.PepperBddIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

class PepperBddComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }

        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        configuration.kotlinSourceRoots.forEach {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "*** Hello from ***" + it.path
            )
        }

        val logging = true
        IrGenerationExtension.registerExtension(
            PepperBddIrGenerationExtension(DebugLogger(debug = logging, messageCollector = messageCollector))
        )
    }
}
