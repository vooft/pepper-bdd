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
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class ElementTransformer(private val pluginContext: IrPluginContext, private val debugLogger: DebugLogger) :
    IrElementTransformerVoidWithContext() {

    private val symbolGiven = pluginContext.findStep("Given")
    private val symbolWhen = pluginContext.findStep("When")
    private val symbolThen = pluginContext.findStep("Then")

    private val dslClass = pluginContext.referenceClass(ClassId(FqName("io.github.vooft.pepper.dsl"), Name.identifier("PepperSpecDsl")))!!

    private val givenContainer = pluginContext.findContainerMethod("GivenContainer")
    private val whenContainer = pluginContext.findContainerMethod("WhenContainer")
    private val thenContainer = pluginContext.findContainerMethod("ThenContainer")

    private val stepAnnotation = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("Step")
            )
        )
    )

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

//        val receiver = pluginContext.referenceClass(ClassId(FqName("io.github.vooft.pepper"), Name.identifier("PepperSpec")))!!
//            .owner.thisReceiver

        /*
      CONSTRUCTOR visibility:public <> () returnType:io.github.vooft.pepper.sample.PepperUnprocessedSpec [primary]
BLOCK_BODY
  DELEGATING_CONSTRUCTOR_CALL 'public constructor <init> (block: @[ExtensionFunctionType] kotlin.Function1<io.github.vooft.pepper.dsl.PepperSpecDsl, kotlin.Unit>) [primary] declared in io.github.vooft.pepper.PepperSpec'
    block: FUN_EXPR type=@[ExtensionFunctionType] kotlin.Function1<io.github.vooft.pepper.dsl.PepperSpecDsl, kotlin.Unit> origin=LAMBDA
      FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> ($receiver:io.github.vooft.pepper.dsl.PepperSpecDsl) returnType:kotlin.Unit
        $receiver: VALUE_PARAMETER name:<this> type:io.github.vooft.pepper.dsl.PepperSpecDsl
        BLOCK_BODY
          VAR name:var1 type:kotlin.String [val]
            CALL 'public final fun GivenContainer <R> (stepName: kotlin.String, block: kotlin.Function0<R of io.github.vooft.pepper.GivenContainer>): R of io.github.vooft.pepper.GivenContainer declared in io.github.vooft.pepper' type=kotlin.String origin=null
              <R>: kotlin.String
              $receiver: GET_VAR '<this>: io.github.vooft.pepper.dsl.PepperSpecDsl declared in io.github.vooft.pepper.sample.PepperUnprocessedSpec.<init>.<anonymous>' type=io.github.vooft.pepper.dsl.PepperSpecDsl origin=null
              stepName: CONST String type=kotlin.String value="test"
              block: FUN_EXPR type=kotlin.Function0<kotlin.String> origin=LAMBDA
                FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> () returnType:kotlin.String
                  BLOCK_BODY
                    RETURN type=kotlin.Nothing from='local final fun <anonymous> (): kotlin.String declared in io.github.vooft.pepper.sample.PepperUnprocessedSpec.<init>.<anonymous>'
                      CALL 'public final fun my test step (): kotlin.String declared in io.github.vooft.pepper.sample' type=kotlin.String origin=null
  INSTANCE_INITIALIZER_CALL classDescriptor='CLASS CLASS name:PepperUnprocessedSpec modality:FINAL visibility:public superTypes:[io.github.vooft.pepper.PepperSpec]'

         */

        val all = allScopes
        val reversed = all.reversed()
        val found = reversed.firstOrNull {
            val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
            val extension = element.extensionReceiverParameter ?: return@firstOrNull false
            element.name.asString() == "<anonymous>" && extension.type.classOrFail == dslClass
        }?.irElement as? IrSimpleFunction ?: error("Cannot find lambda function with PepperSpecDsl receiver")

        return irCall(container).apply {
            this.extensionReceiver = irGet(found.extensionReceiverParameter!!)
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
            return DeclarationIrBuilder(pluginContext, expression.symbol).wrapWithStep(expression, requireNotNull(currentDeclarationParent))
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

private fun IrPluginContext.findStep(name: String) = requireNotNull(
    referenceProperties(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper.dsl"),
            callableName = Name.identifier(name)
        )
    ).single().owner.getter
).symbol

//private fun IrPluginContext.findContainerMethod(name: String) = referenceFunctions(
//    callableId = CallableId(
//        packageName = FqName("io.github.vooft.pepper"),
//        callableName = Name.identifier("PepperSpec.$name")
//    )
//).single().owner

private fun IrPluginContext.findContainerMethod(name: String) = run {
    referenceFunctions(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper"),
            callableName = Name.identifier(name)
        )
    ).single().owner
}
