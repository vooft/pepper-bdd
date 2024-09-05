package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import io.github.vooft.pepper.compiler.transform.StepPrefix.AND
import io.github.vooft.pepper.compiler.transform.StepPrefix.GIVEN
import io.github.vooft.pepper.compiler.transform.StepPrefix.THEN
import io.github.vooft.pepper.compiler.transform.StepPrefix.WHEN
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
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
import java.util.UUID

internal class PepperStepsCollector(private val pluginContext: IrPluginContext, private val debugLogger: DebugLogger) :
    IrElementTransformerVoidWithContext() {

    private val references = PepperReferences(pluginContext)

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

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(references.pepperSpec)) {
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
        if (allScopes.findScenarioDslBlock() == null) {
            return super.visitCall(expression)
        }

        val replacedStep = replaceIfPrefix(expression)
        if (replacedStep != null) {
            return replacedStep
        }

        collectStep(expression)

        return super.visitCall(expression)
    }

    private fun collectStep(expression: IrCall) {
        if (currentClassName == null || !expression.symbol.owner.hasAnnotation(references.stepAnnotation)) {
            return
        }

        val prefix = when (stepIndex++) {
            0 -> currentStepPrefix
            else -> AND
        }

        currentClassSteps.add(
            StepIdentifier(
                id = UUID.randomUUID().toString(),
                prefix = prefix,
                name = expression.symbol.owner.name.asString()
            )
        )
    }

    private fun replaceIfPrefix(expression: IrCall): IrExpression? {
        currentStepPrefix = when (expression.symbol) {
            references.prefixGiven -> GIVEN
            references.prefixWhen -> WHEN
            references.prefixThen -> THEN

            else -> return null
        }

        debugLogger.log("Starting prefix: $currentStepPrefix")
        stepIndex = 0
        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock { }
    }
}
