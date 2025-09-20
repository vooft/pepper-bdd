package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.util.concurrent.atomic.AtomicInteger

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

    private fun IrBuilderWithScope.wrapWithContainer(originalCall: IrCall, currentDeclarationParent: IrDeclarationParent): IrExpression {
        val originalReturnType = originalCall.symbol.owner.returnType

        val lambda = irLambda(
            returnType = originalReturnType,
            lambdaParent = currentDeclarationParent // must have local scope accessible
        ) { +irReturn(originalCall) }

        val parentFunction = allScopes.findScenarioDslBlock(references)
            ?: error("Cannot find lambda function with ${PepperReferences.scenarioDslFqName} receiver")

        debugLogger.log("Wrapping call with StepContainer: ${originalCall.symbol.owner.name}")

        return irBlock {
            // store each argument in a variable and replace them in a call to avoid double-calculation
            val variables = originalCall.replaceValueArgumentsWithVariables(parentFunction)
            variables.forEach { (_, variable) -> +variable }

            // StepContainer("stepId", { originalCall(arg1, arg2) }, mapOf("arg1" to arg1, "arg2" to arg2))
            +irCall(references.stepContainerSymbol).apply {
                this.pepperExtensionReceiver = irGet(requireNotNull(parentFunction.pepperExtensionReceiverParameter))
                typeArguments[0] = originalReturnType

                val currentCall = originalCall.symbol.owner.name.asString()

                require(currentScenario.remainingSteps.isNotEmpty()) { "No steps left for scenario ${currentScenario.scenarioIdentifier}" }
                val step = currentScenario.remainingSteps.removeFirst()
                require(step.name == currentCall) { "Step name mismatch: ${step.name} != $currentCall" }

                arguments[0] = irString(step.id)
                arguments[1] = lambda
                arguments[2] = valueArgumentsToStepArgumentsList(variables)
            }
        }
    }

    /**
     * Generates new variable for each argument and replaces argument with variable.
     */
    private fun IrCall.replaceValueArgumentsWithVariables(outerFunction: IrFunction): Map<String, IrVariable> {
        val result = mutableMapOf<String, IrVariable>()

        // TODO: improve
        for (index in 0..<symbol.owner.pepperValueParameters.size) {
            val parameter = symbol.owner.pepperValueParameters[index]
            val expression = arguments[index] ?: continue

            val variable = IrVariableImpl(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                origin = IrDeclarationOrigin.DEFINED,
                symbol = IrVariableSymbolImpl(),
                name = Name.identifier("pepperVariable$${GENERATED_VARIABLE_COUNTER.getAndIncrement()}"),
                type = parameter.type,
                isVar = false,
                isConst = false,
                isLateinit = false
            )
            variable.parent = outerFunction
            variable.initializer = expression
            arguments[index] = IrGetValueImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                symbol = variable.symbol,
            )

            result[parameter.name.asString()] = variable
        }

        return result.toMap()
    }

    /**
     * Convert value arguments to listOf(StepArgument("arg1", "myType", arg1), ...)
     */
    private fun valueArgumentsToStepArgumentsList(arguments: Map<String, IrVariable>): IrCallImpl {
        val irBuiltIns = pluginContext.irBuiltIns
        val nullableAnyType = irBuiltIns.anyType.makeNullable()
        val stringType = irBuiltIns.stringType

        val stepArgumentConstructorCall = references.stepArgumentClassSymbol.constructors.single()

        val listOfSymbol = pluginContext.referenceFunctions(CallableId(FqName("kotlin.collections"), Name.identifier("listOf")))
            .first { it.owner.pepperValueParameters.size == 1 && it.owner.pepperValueParameters.first().isVararg }
        val argumentsMapType = irBuiltIns.mapClass.typeWith(stringType, nullableAnyType)

        return IrCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = argumentsMapType,
            symbol = listOfSymbol,
            typeArgumentsCount = 2,
            origin = null,
            superQualifierSymbol = null
        ).apply {
            typeArguments[0] = stringType
            typeArguments[1] = nullableAnyType

            this.arguments[0] = IrVarargImpl(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                type = irBuiltIns.arrayClass.typeWith(references.stepArgumentClassSymbol.defaultType),
                varargElementType = references.stepArgumentClassSymbol.defaultType,
                elements = arguments.map { (name, expression) ->
                    IrConstructorCallImpl(
                        startOffset = UNDEFINED_OFFSET,
                        endOffset = UNDEFINED_OFFSET,
                        type = references.stepArgumentClassSymbol.defaultType,
                        symbol = stepArgumentConstructorCall,
                        typeArgumentsCount = 0,
                        constructorTypeArgumentsCount = 0,
                    ).apply {
                        this.arguments[0] = name.toIrConst(stringType)

                        // TODO: improve generics type resolution
                        this.arguments[1] = (expression.type.classFqName?.asString() ?: "null").toIrConst(stringType)
                        this.arguments[2] = IrGetValueImpl(
                            startOffset = expression.startOffset,
                            endOffset = expression.endOffset,
                            type = nullableAnyType,
                            symbol = expression.symbol,
                        )
                    }
                }
            )
        }
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

    companion object {
        private val GENERATED_VARIABLE_COUNTER = AtomicInteger()
    }
}
