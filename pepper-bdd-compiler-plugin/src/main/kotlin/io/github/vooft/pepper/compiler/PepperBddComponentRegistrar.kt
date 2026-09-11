package io.github.vooft.pepper.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class PepperBddComponentRegistrar : CompilerPluginRegistrar() {

    override val pluginId: String = "io.github.vooft.pepper-bdd-compiler"

    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }

        val logging = true
        IrGenerationExtension.registerExtension(
            PepperBddIrGenerationExtension(DebugLogger(debug = logging, configuration = configuration))
        )
    }
}
