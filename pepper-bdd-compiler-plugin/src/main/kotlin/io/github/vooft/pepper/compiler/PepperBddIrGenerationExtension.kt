package io.github.vooft.pepper.compiler

import io.github.vooft.pepper.compiler.transform.PepperStepContainerWrapper
import io.github.vooft.pepper.compiler.transform.PepperStepsAdder
import io.github.vooft.pepper.compiler.transform.PepperStepsCollector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class PepperBddIrGenerationExtension(private val debugLogger: DebugLogger) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
//        debugLogger.log("generate() before: ${moduleFragment.dump()}")
        val stepsCollector = PepperStepsCollector(pluginContext, debugLogger)
        moduleFragment.transform(stepsCollector, null)
        debugLogger.log("Steps: ${stepsCollector.steps}")

        val steps = stepsCollector.steps
        if (steps.isNotEmpty()) {
            moduleFragment.transform(PepperStepContainerWrapper(stepsCollector.steps, pluginContext, debugLogger), null)
            moduleFragment.transform(PepperStepsAdder(stepsCollector.steps, pluginContext, debugLogger), null)
        }

//        debugLogger.log("generate() after: ${moduleFragment.dump()}")
    }
}
