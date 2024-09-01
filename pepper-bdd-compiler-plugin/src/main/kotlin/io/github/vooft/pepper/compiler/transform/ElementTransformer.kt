package io.github.vooft.pepper.compiler.transform

import io.github.vooft.pepper.compiler.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class ElementTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    fun IrBlockBuilder.createLambdaBody(irCall: IrCall) {
        val printlnFunction = pluginContext.referenceFunctions(CallableId(FqName("kotlin.io"), Name.identifier("println")))
            .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters.single().type == pluginContext.irBuiltIns.anyNType }

        +irCall(printlnFunction).apply {
            putValueArgument(0, irString(irCall.toString()))
        }

        +irCall
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun IrBlockBuilder.wrapIrCallWithRunBlock(irCall: IrCall) {
        val runFunction = pluginContext.referenceFunctions(CallableId(FqName("kotlin"), Name.identifier("run")))
            .single { it.owner.extensionReceiverParameter == null }
        +irCall(runFunction).apply {
            putValueArgument(0, DeclarationIrBuilder(pluginContext, irCall.symbol).irBlock { createLambdaBody(irCall) })
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitCall(expression: IrCall): IrExpression {
        debugLogger.log("visitCall() expression: ${expression.symbol}")
        debugLogger.log("visitCall() hasAnnotation: ${expression.symbol.owner.hasAnnotation(FqName("io.github.vooft.pepper.sample.Test"))}")
        debugLogger.log("visitCall() dump: ${expression.dump()}")

        if (expression.symbol.descriptor.name.asString().startsWith("test")) {
            return DeclarationIrBuilder(pluginContext, expression.symbol).irBlock {
                wrapIrCallWithRunBlock(expression)
            }
        }

//        return irBlock {
//            +irCall(expression.context.irBuiltIns.printlnSymbol).apply {
//                putValueArgument(0, irString("myfun"))
//            }
//            +irReturn(irCall)
//        }

        expression.transform(CreateFuncTransformer(pluginContext, debugLogger), null)
        return super.visitCall(expression)
    }
}
