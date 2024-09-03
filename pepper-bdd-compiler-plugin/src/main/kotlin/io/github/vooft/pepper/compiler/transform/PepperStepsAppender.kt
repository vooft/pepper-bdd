package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass

internal class PepperStepsAppender(
    private val steps: Map<String, List<StepIdentifier>>,
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val addStepFunction = pluginContext.findHelper("addStep")

    private val pepperSpecClass = pluginContext.findPepperSpec()
    private val pepperSpecDslClass = pluginContext.findPepperSpecDsl()
    private var currentClassSteps = listOf<StepIdentifier>()

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(pepperSpecClass)) {
            return super.visitConstructor(declaration)
        }

        currentClassSteps = steps[type.classFqName?.asString()] ?: listOf()

        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (currentClassSteps.isNotEmpty() &&
            expression.symbol.owner.name.asString() == "Scenario" &&
            expression.dispatchReceiver?.type?.classFqName?.asString() == "io.github.vooft.pepper.dsl.PepperSpecDsl"
        ) {
            debugLogger.log("Processing scenario call")

            val parentFunction = allScopes.reversed().firstOrNull {
                val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
                val extension = element.extensionReceiverParameter ?: return@firstOrNull false
                element.name.asString() == "<anonymous>" && extension.type.classOrFail == pepperSpecDslClass
            }?.irElement as? IrSimpleFunction ?: error("Cannot find lambda function with $pepperSpecDslClass receiver")

            return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock {
                for (step in currentClassSteps) {
                    +irCall(addStepFunction).apply {
                        this.extensionReceiver = irGet(requireNotNull(parentFunction.extensionReceiverParameter))

                        putValueArgument(0, irString(step.id.toString()))
                        putValueArgument(1, irString(step.name))
                    }
                }

                +expression
            }
        }

        return super.visitCall(expression)
    }
}
