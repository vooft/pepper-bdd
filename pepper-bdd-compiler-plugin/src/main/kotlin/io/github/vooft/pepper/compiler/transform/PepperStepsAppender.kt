package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class PepperStepsAppender(
    private val steps: Map<String, List<StepIdentifier>>,
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.name.asString() == "Scenario"
            && expression.dispatchReceiver?.type?.classFqName?.asString() == "io.github.vooft.pepper.dsl.PepperSpecDsl") {

            val println = pluginContext.referenceFunctions(CallableId(FqName("kotlin.io"), Name.identifier("println")))
                .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters.single().type.isNullableAny() }
                .owner

            return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock {
                +irCall(println).apply {
                    putValueArgument(0, irString("abyrvalg"))
                }
                +expression
            }
        }

        return super.visitCall(expression)
    }
}
