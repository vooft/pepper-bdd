package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import io.github.vooft.pepper.compiler.transform.StepType.GIVEN
import io.github.vooft.pepper.compiler.transform.StepType.THEN
import io.github.vooft.pepper.compiler.transform.StepType.WHEN
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class ElementTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val symbolGiven = pluginContext.findStep("Given")
    private val symbolWhen = pluginContext.findStep("When")
    private val symbolThen = pluginContext.findStep("Then")

    private val givenContainer = pluginContext.findContainerMethod("GivenContainer")
    private val whenContainer = pluginContext.findContainerMethod("WhenContainer")
    private val thenContainer = pluginContext.findContainerMethod("ThenContainer")

    private val stepAnnotation = pluginContext.referenceClass(ClassId(FqName("io.github.vooft.pepper"), Name.identifier("Step")))!!

    private var currentStep: StepType = GIVEN

    fun IrBuilderWithScope.wrapWithStep(originalCall: IrCall, currentDeclarationParent: IrDeclarationParent): IrFunctionAccessExpression {
        val originalReturnType = originalCall.symbol.owner.returnType

        val lambda = irLambda(
            returnType = originalReturnType,
            lambdaType = pluginContext.irBuiltIns.functionN(0).typeWith(originalReturnType),
            lambdaParent = currentDeclarationParent // must have local scope accessible
        ) { +irReturn(originalCall) }

        val container = when (currentStep) {
            GIVEN -> givenContainer
            WHEN -> whenContainer
            THEN -> thenContainer
        }
        return irCall(container).apply {
            putTypeArgument(0, originalReturnType)
            putValueArgument(0, irString(originalCall.symbol.owner.name.asString()))
            putValueArgument(
                index = 1,
                valueArgument = lambda
            )
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitCall(expression: IrCall): IrExpression {
        debugLogger.log("visitCall() expression: ${expression.symbol}")
        debugLogger.log("visitCall() name: ${expression.symbol.descriptor.name}")
        debugLogger.log("visitCall() hasAnnotation: ${expression.symbol.owner.hasAnnotation(stepAnnotation)}")
        debugLogger.log("visitCall() dump: ${expression.dump()}")

        val replacedStep = replaceIfStep(expression)
        if (replacedStep != null) {
            return replacedStep
        }

        if (expression.symbol.owner.hasAnnotation(stepAnnotation)) {
            val currentDeclaration = requireNotNull(currentScope?.irElement as? IrDeclaration)
            return DeclarationIrBuilder(pluginContext, expression.symbol).wrapWithStep(expression, currentDeclaration.parent)
        }

        return super.visitCall(expression)
    }

    private fun replaceIfStep(expression: IrCall): IrExpression? {
        when (expression.symbol) {
            symbolGiven -> {
                currentStep = GIVEN
                debugLogger.log("visitCall() Given")
            }

            symbolWhen -> {
                currentStep = WHEN
                debugLogger.log("visitCall() When")
            }

            symbolThen -> {
                currentStep = THEN
                debugLogger.log("visitCall() Then")
            }

            else -> return null
        }

        return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock { }
    }
}

enum class StepType {
    GIVEN,
    WHEN,
    THEN
}

private fun IrPluginContext.findStep(name: String) = referenceProperties(
    callableId = CallableId(
        packageName = FqName("io.github.vooft.pepper.dsl"),
        callableName = Name.identifier(name)
    )
).single().owner.getter!!.symbol

private fun IrPluginContext.findContainerMethod(name: String) = referenceFunctions(
    callableId = CallableId(
        packageName = FqName("io.github.vooft.pepper.dsl"),
        callableName = Name.identifier(name)
    )
).single().owner
