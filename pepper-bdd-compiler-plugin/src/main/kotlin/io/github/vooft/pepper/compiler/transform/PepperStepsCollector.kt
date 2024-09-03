package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import io.github.vooft.pepper.compiler.transform.StepPrefix.AND
import io.github.vooft.pepper.compiler.transform.StepPrefix.GIVEN
import io.github.vooft.pepper.compiler.transform.StepPrefix.THEN
import io.github.vooft.pepper.compiler.transform.StepPrefix.WHEN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.util.UUID

internal class PepperStepsCollector(private val pluginContext: IrPluginContext, private val debugLogger: DebugLogger) :
    IrElementTransformerVoid() {

    private val symbolGiven = pluginContext.findStep("Given")
    private val symbolWhen = pluginContext.findStep("When")
    private val symbolThen = pluginContext.findStep("Then")

    private var currentClassName: String? = null
    private val currentClassSteps = mutableListOf<StepIdentifier>()

    private var currentStepPrefix: StepPrefix = GIVEN
    private var stepIndex = 0

    private val visitedClasses = mutableMapOf<String, List<StepIdentifier>>()
    val steps: Map<String, List<StepIdentifier>>
        get() {
            val current = currentClassName?.let { mapOf(it to currentClassSteps.toList()) } ?: mapOf()
            return (current + visitedClasses).filterValues { it.isNotEmpty() }
        }

    private val stepAnnotation = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("Step")
            )
        )
    )

    private val pepperSpecClass = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("PepperSpec")
            )
        )
    )

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(pepperSpecClass)) {
            return super.visitConstructor(declaration)
        }

        currentClassName?.let {
            visitedClasses[it] = currentClassSteps.toList()
            currentClassSteps.clear()
        }

        currentClassName = type.classFqName?.asString()
        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val replacedStep = replaceIfStep(expression)
        if (replacedStep != null) {
            return replacedStep
        }

        if (currentClassName != null && expression.symbol.owner.hasAnnotation(stepAnnotation)) {
            val prefix = when (stepIndex++) {
                0 -> currentStepPrefix
                else -> AND
            }

            currentClassSteps.add(
                StepIdentifier(
                    id = UUID.randomUUID(),
                    prefix = prefix,
                    name = expression.symbol.owner.name.asString()
                )
            )
        }

        return super.visitCall(expression)
    }

    private fun replaceIfStep(expression: IrCall): IrExpression? {
        currentStepPrefix = when (expression.symbol) {
            symbolGiven -> GIVEN
            symbolWhen -> WHEN
            symbolThen -> THEN

            else -> return null
        }

        debugLogger.log("Starting prefix: $currentStepPrefix")
        stepIndex = 0
        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock { }
    }
}

private fun IrPluginContext.findStep(name: String) = requireNotNull(
    referenceProperties(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper.dsl"),
            callableName = Name.identifier(name)
        )
    ).single().owner.getter
).symbol
