package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.getAllArgumentsWithIr
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class PepperStepContainerWrapper(
    private val steps: Map<ScenarioIdentifier, List<StepIdentifier>>,
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    private val references = PepperReferences(pluginContext)

    private var currentScenario = CurrentScenarioStorage(null)

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(references.pepperSpecSymbol)) {
            return super.visitConstructor(declaration)
        }

        currentScenario = CurrentScenarioStorage(type.classFqName?.let { ClassName(it.asString()) })

        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (currentScenario.className == null) {
            return super.visitCall(expression)
        }

        val scenarioTitle = expression.findScenarioTitle()
        if (scenarioTitle != null) {
            currentScenario = CurrentScenarioStorage(currentScenario.className)
            currentScenario.scenarionTitle = ScenarioTitle(scenarioTitle)
            currentScenario.remainingSteps.addAll(steps[currentScenario.scenarioIdentifier] ?: emptyList())
            return super.visitCall(expression)
        }

        if (!expression.symbol.owner.hasAnnotation(references.stepAnnotationSymbol) || allScopes.findScenarioDslBlock(references) == null) {
            return super.visitCall(expression)
        }

        return DeclarationIrBuilder(
            pluginContext,
            expression.symbol
        ).wrapWithContainer(expression, requireNotNull(currentDeclarationParent))
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

        val parentFunction = allScopes.findScenarioDslBlock(references)
            ?: error("Cannot find lambda function with ${PepperReferences.scenarioDslFqName} receiver")

        debugLogger.log("Wrapping call with StepContainer: ${originalCall.symbol.owner.name}")

        return irCall(references.stepContainerSymbol).apply {
            this.extensionReceiver = irGet(requireNotNull(parentFunction.extensionReceiverParameter))
            putTypeArgument(0, originalReturnType)

            val currentCall = originalCall.symbol.owner.name.asString()

            require(currentScenario.remainingSteps.isNotEmpty()) { "No steps left for scenario ${currentScenario.scenarioIdentifier}" }
            val step = currentScenario.remainingSteps.removeFirst()
            require(step.name == currentCall) { "Step name mismatch: ${step.name} != $currentCall" }

            putValueArgument(index = 0, valueArgument = irString(step.id))
            putValueArgument(index = 1, valueArgument = lambda)
            putValueArgument(index = 2, valueArgument = originalCall.valueArgumentsToMap())
        }
    }

    private fun IrCall.valueArgumentsToMap(): IrCallImpl {
        val irBuiltIns = pluginContext.irBuiltIns
        val nullableAnyType = irBuiltIns.anyType.makeNullable()
        val stringType = irBuiltIns.stringType

        val pairClassId = ClassId(FqName("kotlin"), Name.identifier("Pair"))
        val argumentPairType = pluginContext.referenceClass(pairClassId)?.typeWith(stringType, nullableAnyType) ?: nullableAnyType
        val pairConstructorCall = pluginContext.referenceConstructors(pairClassId)
            .first { it.owner.valueParameters.size == 2 }

        val mapOfCallableId = CallableId(FqName("kotlin.collections"), Name.identifier("mapOf"))
        val mapOfSymbol = pluginContext.referenceFunctions(mapOfCallableId)
            .first { it.owner.valueParameters.size == 1 && it.owner.valueParameters.first().isVararg }
        val argumentsMapType = irBuiltIns.mapClass.typeWith(stringType, nullableAnyType)

        val allArguments = getAllArgumentsWithIr()

        return IrCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = argumentsMapType,
            symbol = mapOfSymbol,
            typeArgumentsCount = 2,
            valueArgumentsCount = 1,
            origin = null,
            superQualifierSymbol = null
        ).apply {
            putTypeArgument(0, stringType)
            putTypeArgument(1, nullableAnyType)

            putValueArgument(
                index = 0,
                valueArgument = IrVarargImpl(
                    startOffset = UNDEFINED_OFFSET,
                    endOffset = UNDEFINED_OFFSET,
                    type = irBuiltIns.arrayClass.typeWith(argumentPairType),
                    varargElementType = argumentPairType,
                    elements = listOf())
//                    allArguments.map { (valueParameter, expression) ->
//                        IrConstructorCallImpl(
//                            startOffset = UNDEFINED_OFFSET, endOffset = UNDEFINED_OFFSET,
//                            type = argumentPairType,
//                            symbol = pairConstructorCall,
//                            typeArgumentsCount = 2,
//                            constructorTypeArgumentsCount = 0,
//                            valueArgumentsCount = 2,
//                        ).apply {
//                            putTypeArgument(0, stringType)
//                            putTypeArgument(1, nullableAnyType)
//
//                            debugLogger.log("Adding argument: ${valueParameter.name} = $expression")
//                            putValueArgument(0, valueParameter.name.asString().toIrConst(stringType))
//                            putValueArgument(1, expression)
//                        }
//                    })
            )
        }.also { debugLogger.log("Arguments map: ${it.dump()}") }
    }

    private class CurrentScenarioStorage(val className: ClassName?) {
        var scenarionTitle: ScenarioTitle? = null
        val remainingSteps: MutableList<StepIdentifier> = mutableListOf()
    }

    private val CurrentScenarioStorage.scenarioIdentifier: ScenarioIdentifier?
        get() {
            val className = className ?: return null
            val scenarioTitle = scenarionTitle ?: return null

            return ScenarioIdentifier(className, scenarioTitle)
        }
}
