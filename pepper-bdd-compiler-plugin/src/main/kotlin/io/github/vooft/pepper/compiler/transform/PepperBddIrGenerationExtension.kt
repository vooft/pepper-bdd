package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class PepperBddIrGenerationExtension(private val debugLogger: DebugLogger) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
//        debugLogger.log("generate() before: ${moduleFragment.dump()}")
        moduleFragment.transform(PepperContainerTransformer(pluginContext, debugLogger), null)
//        debugLogger.log("generate() after: ${moduleFragment.dump()}")
    }
}
