package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class PepperStepsCollector(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val symbolGiven = pluginContext.findStep("Given")
    private val symbolWhen = pluginContext.findStep("When")
    private val symbolThen = pluginContext.findStep("Then")

    private val scenarioDslClass = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper.dsl"),
                topLevelName = Name.identifier("ScenarioDsl")
            )
        )
    )

    private val givenContainer = pluginContext.findContainerMethod("GivenContainer")
    private val whenContainer = pluginContext.findContainerMethod("WhenContainer")
    private val thenContainer = pluginContext.findContainerMethod("ThenContainer")
    private val andContainer = pluginContext.findContainerMethod("AndContainer")

    private val stepAnnotation = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("Step")
            )
        )
    )

    private var currentStep: StepType = GIVEN
    private var stepIndex = 0

    private fun IrBuilderWithScope.wrapWithStep(
        originalCall: IrCall,
        currentDeclarationParent: IrDeclarationParent
    ): IrFunctionAccessExpression {
        val originalReturnType = originalCall.symbol.owner.returnType

        val lambda = irLambda(
            returnType = originalReturnType,
            lambdaParent = currentDeclarationParent // must have local scope accessible
        ) { +irReturn(originalCall) }

        val container = when {
            stepIndex > 0 -> andContainer
            else -> when (currentStep) {
                GIVEN -> givenContainer
                WHEN -> whenContainer
                THEN -> thenContainer
            }
        }

        stepIndex++

        val found = allScopes.reversed().firstOrNull {
            val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
            val extension = element.extensionReceiverParameter ?: return@firstOrNull false
            element.name.asString() == "<anonymous>" && extension.type.classOrFail == scenarioDslClass
        }?.irElement as? IrSimpleFunction ?: error("Cannot find lambda function with $scenarioDslClass receiver")

        return irCall(container).apply {
            this.extensionReceiver = irGet(requireNotNull(found.extensionReceiverParameter))
            putTypeArgument(0, originalReturnType)
            putValueArgument(0, irString(originalCall.symbol.owner.name.asString()))
            putValueArgument(
                index = 1,
                valueArgument = lambda
            )
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val replacedStep = replaceIfStep(expression)
        if (replacedStep != null) {
            return replacedStep
        }

        if (expression.symbol.owner.hasAnnotation(stepAnnotation)) {
            return DeclarationIrBuilder(pluginContext, expression.symbol).wrapWithStep(expression, requireNotNull(currentDeclarationParent))
        }

        return super.visitCall(expression)
    }

    private fun replaceIfStep(expression: IrCall): IrExpression? {
        currentStep = when (expression.symbol) {
            symbolGiven -> GIVEN
            symbolWhen -> WHEN
            symbolThen -> THEN

            else -> return null
        }

        debugLogger.log("Starting step: $currentStep")
        stepIndex = 0
        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock { }
    }
}

enum class StepType {
    GIVEN,
    WHEN,
    THEN
}

private fun IrPluginContext.findStep(name: String) = requireNotNull(
    referenceProperties(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper.dsl"),
            callableName = Name.identifier(name)
        )
    ).single().owner.getter
).symbol

private fun IrPluginContext.findContainerMethod(name: String) = run {
    referenceFunctions(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper"),
            callableName = Name.identifier(name)
        )
    ).single().owner
}
