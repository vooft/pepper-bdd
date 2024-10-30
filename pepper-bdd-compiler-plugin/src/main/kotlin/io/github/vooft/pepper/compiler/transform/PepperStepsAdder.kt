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

internal class PepperStepsAdder(
    private val steps: Map<String, List<StepIdentifier>>,
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val references = PepperReferences(pluginContext)

    private var currentClassSteps = listOf<StepIdentifier>()

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(references.pepperSpec)) {
            return super.visitConstructor(declaration)
        }

        currentClassSteps = steps[type.classFqName?.asString()] ?: listOf()

        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        // here we only check the Scenario method call
        if (currentClassSteps.isEmpty()) {
            return super.visitCall(expression)
        }

        val scenarioTitle = expression.findScenarioTitle() ?: return super.visitCall(expression)

        debugLogger.log("Processing scenario call $scenarioTitle")

        val parentFunction = allScopes.reversed().firstOrNull {
            val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
            val extension = element.extensionReceiverParameter ?: return@firstOrNull false
            element.name.asString() == "<anonymous>" && extension.type.classOrFail == references.pepperSpecDsl
        }?.irElement as? IrSimpleFunction ?: error("Cannot find lambda function with ${references.pepperSpecDsl} receiver")

        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock {
            val stepIndexLength = currentClassSteps.size.toString().length
            for ((index, step) in currentClassSteps.withIndex()) {
                +irCall(references.addStep).apply {
                    this.extensionReceiver = irGet(requireNotNull(parentFunction.extensionReceiverParameter))

                    val prefix = listOf(
                        (index + 1).toString().padStart(stepIndexLength, '0'),
                        ". ",
                        step.prefix.capitalized
                    ).joinToString("")

                    putValueArgument(0, irString(scenarioTitle))
                    putValueArgument(1, irString(step.id))
                    putValueArgument(2, irString(prefix))
                    putValueArgument(3, irString(step.name))
                }
            }

            +expression
        }
    }
}

private val StepPrefix.capitalized: String
    get() {
        val first = name.first().uppercase()
        val rest = name.drop(1).lowercase()
        return first + rest
    }
