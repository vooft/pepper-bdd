package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import io.github.vooft.pepper.compiler.transform.StepType.GIVEN
import io.github.vooft.pepper.compiler.transform.StepType.THEN
import io.github.vooft.pepper.compiler.transform.StepType.WHEN
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

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

    fun IrBlockBuilder.prefixWithPrintln(irCall: IrCall) {
        val originalReturnType = irCall.symbol.owner.returnType
        val lambda = pluginContext.irFactory.buildFun {
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            name = SpecialNames.NO_NAME_PROVIDED
            visibility = DescriptorVisibilities.LOCAL
            returnType = originalReturnType
            modality = Modality.FINAL
        }.apply {
            parent = irCall.symbol.owner.parent
            body = DeclarationIrBuilder(pluginContext, irCall.symbol).irBlockBody {
                +irCall
            }
        }

        +irCall(givenContainer).apply {
            putValueArgument(
                index = 0,
                valueArgument = IrFunctionExpressionImpl(
                    startOffset = UNDEFINED_OFFSET,
                    endOffset = UNDEFINED_OFFSET,
                    type = pluginContext.irBuiltIns.functionN(0).typeWith(originalReturnType),
                    function = lambda,
                    origin = IrStatementOrigin.LAMBDA
                )
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
            return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock {
                prefixWithPrintln(expression)
            }
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
