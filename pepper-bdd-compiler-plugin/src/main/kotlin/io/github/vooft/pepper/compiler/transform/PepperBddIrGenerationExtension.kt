package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class PepperBddIrGenerationExtension(private val debugLogger: DebugLogger) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
//        debugLogger.log("generate() before: ${moduleFragment.dump()}")
        val stepsCollector = PepperStepsCollector(pluginContext)
        moduleFragment.transform(stepsCollector, null)
        debugLogger.log("Steps: ${stepsCollector.steps}")

        moduleFragment.transform(PepperContainerTransformer(stepsCollector.steps, pluginContext, debugLogger), null)

        moduleFragment.transform(PepperStepsAppender(stepsCollector.steps, pluginContext, debugLogger), null)
//        debugLogger.log("generate() after: ${moduleFragment.dump()}")
    }
}
