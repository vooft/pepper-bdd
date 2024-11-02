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

internal class PepperRemainingStepsAdder(
    private val steps: Map<ScenarioIdentifier, List<StepIdentifier>>,
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val references = PepperReferences(pluginContext)

    private var currentClassName: ClassName? = null
    private var currentScenarioSteps = listOf<StepIdentifier>()

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(references.pepperSpecSymbol)) {
            return super.visitConstructor(declaration)
        }

        currentClassName = type.classFqName?.let { ClassName(it.asString()) }

        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val className = currentClassName ?: return super.visitCall(expression)

        val scenarioTitle = expression.findScenarioTitle() ?: return super.visitCall(expression)
        currentScenarioSteps = steps[ScenarioIdentifier(className, ScenarioTitle(scenarioTitle))] ?: listOf()

        // here we only check the Scenario method call
        if (currentScenarioSteps.isEmpty()) {
            return super.visitCall(expression)
        }

        debugLogger.log("Adding remaining steps to scenario $scenarioTitle")

        val parentFunction = allScopes.reversed().firstOrNull {
            val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
            val extension = element.extensionReceiverParameter ?: return@firstOrNull false
            element.name.asString() == "<anonymous>" && extension.type.classOrFail == references.pepperSpecDslSymbol
        }?.irElement as? IrSimpleFunction ?: error("Cannot find lambda function with ${references.pepperSpecDslSymbol} receiver")

        debugLogger.log("Adding steps to scenario inside $parentFunction")

        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock {
            val stepIndexLength = currentScenarioSteps.size.toString().length
            for ((index, step) in currentScenarioSteps.withIndex()) {
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
