package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class PepperStepContainerWrapper(
    private val steps: Map<String, List<StepIdentifier>>,
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val scenarioDslClass = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper.dsl"),
                topLevelName = Name.identifier("ScenarioDsl")
            )
        )
    )

    private val stepContainer = pluginContext.findHelper("StepContainer")
    private val stepAnnotation = pluginContext.findStepAnnotation()
    private val pepperSpecClass = pluginContext.findPepperSpec()

    private val remainingSteps: MutableList<StepIdentifier> = mutableListOf()

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(pepperSpecClass)) {
            return super.visitConstructor(declaration)
        }

        remainingSteps.clear()
        remainingSteps.addAll(steps[type.classFqName?.asString()] ?: listOf())

        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.hasAnnotation(stepAnnotation)) {
            return DeclarationIrBuilder(
                pluginContext,
                expression.symbol
            ).wrapWithContainer(expression, requireNotNull(currentDeclarationParent))
        }

        return super.visitCall(expression)
    }

    private fun IrBuilderWithScope.wrapWithContainer(
        originalCall: IrCall,
        currentDeclarationParent: IrDeclarationParent
    ): IrFunctionAccessExpression {
        val originalReturnType = originalCall.symbol.owner.returnType

        val lambda = irLambda(
            returnType = originalReturnType,
            lambdaParent = currentDeclarationParent // must have local scope accessible
        ) { +irReturn(originalCall) }

        val parentFunction = allScopes.reversed().firstOrNull {
            val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
            val extension = element.extensionReceiverParameter ?: return@firstOrNull false
            element.name.asString() == "<anonymous>" && extension.type.classOrFail == scenarioDslClass
        }?.irElement as? IrSimpleFunction ?: error("Cannot find lambda function with $scenarioDslClass receiver")

        debugLogger.log("Wrapping call with StepContainer: ${originalCall.symbol.owner.name}")

        return irCall(stepContainer).apply {
            this.extensionReceiver = irGet(requireNotNull(parentFunction.extensionReceiverParameter))
            putTypeArgument(0, originalReturnType)

            val currentCall = originalCall.symbol.owner.name.asString()

            val step = remainingSteps.removeFirst()
            require(step.name == currentCall) { "Step name mismatch: ${step.name} != $currentCall" }

            putValueArgument(0, irString(step.id.toString()))
            putValueArgument(
                index = 1,
                valueArgument = lambda
            )
        }
    }
}