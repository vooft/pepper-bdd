package io.github.vooft.pepper.compiler

import io.github.vooft.pepper.compiler.transform.PepperRemainingStepsAdder
import io.github.vooft.pepper.compiler.transform.PepperScenarioStepsCollector
import io.github.vooft.pepper.compiler.transform.PepperStepContainerWrapper
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class PepperBddIrGenerationExtension(private val debugLogger: DebugLogger) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
//        debugLogger.log("generate() before: ${moduleFragment.dump()}")

        if (pluginContext.referenceClass(ClassId(FqName("io.github.vooft.pepper"), Name.identifier("PepperSpec"))) == null) {
            debugLogger.log("PepperSpec not found")
            return
        }

        val stepsCollector = PepperScenarioStepsCollector(pluginContext, debugLogger)
        moduleFragment.transform(stepsCollector, null)
        debugLogger.log("Steps: ${stepsCollector.steps}")

        val steps = stepsCollector.steps
        if (steps.isNotEmpty()) {
            moduleFragment.transform(PepperStepContainerWrapper(stepsCollector.steps, pluginContext, debugLogger), null)
            moduleFragment.transform(PepperRemainingStepsAdder(stepsCollector.steps, pluginContext, debugLogger), null)
        }

//        debugLogger.log("generate() after: ${moduleFragment.dump()}")
    }
}
