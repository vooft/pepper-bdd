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

internal class PepperScenarioStepsCollector(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val references = PepperReferences(pluginContext)

    private var currentScenario = CurrentScenarioStorage(null)

    private val visitedScenarios = mutableMapOf<ScenarioIdentifier, List<StepIdentifier>>()
    val steps: Map<ScenarioIdentifier, List<StepIdentifier>>
        get() {
            val current = currentScenario.scenarioIdentifier
                ?.let { mapOf(it to currentScenario.steps.toList()) } ?: mapOf()
            return (current + visitedScenarios).filterValues { it.isNotEmpty() }
        }

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(references.pepperSpecSymbol)) {
            return super.visitConstructor(declaration)
        }

        currentScenario.scenarioIdentifier?.let { visitedScenarios[it] = currentScenario.steps.toList() }
        currentScenario = CurrentScenarioStorage(type.classFqName?.let { ClassName(it.asString()) })

        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (currentScenario.className == null) {
            return super.visitCall(expression)
        }

        val scenarioTitle = expression.findScenarioTitle()
        if (scenarioTitle != null) {
            currentScenario.scenarioIdentifier?.let { visitedScenarios[it] = currentScenario.steps.toList() }

            currentScenario = CurrentScenarioStorage(currentScenario.className)
            currentScenario.scenarionTitle = ScenarioTitle(scenarioTitle)
            return super.visitCall(expression)
        }

        if (allScopes.findScenarioDslBlock(references) == null) {
            return super.visitCall(expression)
        }

        val replacedStep = currentScenario.replaceIfPrefix(expression)
        if (replacedStep != null) {
            return replacedStep
        }

        currentScenario.collectStep(expression)

        return super.visitCall(expression)
    }

    private fun CurrentScenarioStorage.collectStep(expression: IrCall) {
        if (!expression.symbol.owner.hasAnnotation(references.stepAnnotationSymbol)) {
            return
        }

        val prefix = when (prefixStepIndex++) {
            0 -> stepPrefix
            else -> AND
        }

        steps.add(
            StepIdentifier(
                id = UUID.randomUUID().toString(),
                prefix = prefix,
                name = expression.symbol.owner.name.asString()
            )
        )
    }

    private fun CurrentScenarioStorage.replaceIfPrefix(expression: IrCall): IrExpression? {
        stepPrefix = when (expression.symbol) {
            references.prefixGiven -> GIVEN
            references.prefixWhen -> WHEN
            references.prefixThen -> THEN

            else -> return null
        }

        debugLogger.log("Starting prefix: $stepPrefix")
        prefixStepIndex = 0
        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock { }
    }

    private class CurrentScenarioStorage(val className: ClassName? = null) {
        var scenarionTitle: ScenarioTitle? = null

        val steps = mutableListOf<StepIdentifier>()

        var stepPrefix: StepPrefix = GIVEN
        var prefixStepIndex = 0
    }

    private val CurrentScenarioStorage.scenarioIdentifier: ScenarioIdentifier?
        get() {
            val className = className ?: return null
            val scenarioTitle = scenarionTitle ?: return null

            return ScenarioIdentifier(className, scenarioTitle)
        }
}
