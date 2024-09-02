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

    private val stepAnnotation = pluginContext.referenceClass(ClassId(FqName("io.github.vooft.pepper"), Name.identifier("Step")))!!

    private var currentStep: StepType? = null

//    fun IrBlockBuilder.createLambdaBody(irCall: IrCall) {
//        val printlnFunction = pluginContext.referenceFunctions(CallableId(FqName("kotlin.io"), Name.identifier("println")))
//            .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters.single().type == pluginContext.irBuiltIns.anyNType }
//
//        +irCall(printlnFunction).apply {
//            putValueArgument(0, irString(irCall.symbol.owner.name.asString()))
//        }
//
//        +irCall
//    }

    /*
BLOCK_BODY
CALL 'public final fun GivenContainer <R> (block: kotlin.Function0<R of io.github.vooft.pepper.dsl.GivenContainer>): R of io.github.vooft.pepper.dsl.GivenContainer declared in io.github.vooft.pepper.dsl' type=kotlin.Unit origin=null
<R>: kotlin.Unit
block: FUN_EXPR type=kotlin.Function0<kotlin.Unit> origin=LAMBDA
  FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> () returnType:kotlin.Unit
    BLOCK_BODY
      CALL 'public final fun my test step (): kotlin.Unit declared in io.github.vooft.pepper.sample' type=kotlin.Unit origin=null
     */
    fun IrBuilderWithScope.prefixWithPrintln(originalCall: IrCall): IrFunctionAccessExpression {
        val originalReturnType = originalCall.symbol.owner.returnType

        val currentDeclaration = requireNotNull(currentScope?.irElement as? IrDeclaration)

        val lambda = irLambda(
            returnType = originalReturnType,
            lambdaType = pluginContext.irBuiltIns.functionN(0).typeWith(originalReturnType),
            lambdaParent = currentDeclaration.parent // must have local scope accessible
        ) {
            +irReturn(
                irCall(originalCall.symbol).apply {
                    for (i in 0 until originalCall.typeArgumentsCount) {
                        putTypeArgument(i, originalCall.getTypeArgument(i))
                    }

                    for (i in 0 until originalCall.valueArgumentsCount) {
                        putValueArgument(i, originalCall.getValueArgument(i))
                    }
                }
            )
        }

        return irCall(givenContainer).apply {
            putTypeArgument(0, originalReturnType)
            putValueArgument(0, irString(originalCall.symbol.owner.name.asString()))
            putValueArgument(
                index = 1,
                valueArgument = lambda
            )
        }
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
            return DeclarationIrBuilder(pluginContext, expression.symbol).prefixWithPrintln(expression)
        }


//        return irBlock {
//            +irCall(expression.context.irBuiltIns.printlnSymbol).apply {
//                putValueArgument(0, irString("myfun"))
//            }
//            +irReturn(irCall)
//        }

        return super.visitCall(expression)
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
