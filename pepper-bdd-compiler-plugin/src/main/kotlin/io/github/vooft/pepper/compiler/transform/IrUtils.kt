package io.github.vooft.pepper.compiler.transform

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.name.Name

fun IrBuilderWithScope.irLambda(
    returnType: IrType,
    lambdaType: IrType,
    lambdaParent: IrDeclarationParent,
    startOffset: Int = this.startOffset,
    endOffset: Int = this.endOffset,
    block: IrBlockBodyBuilder.() -> Unit,
): IrFunctionExpression {
    val lambda = context.irFactory.buildFun {
        this.startOffset = startOffset
        this.endOffset = endOffset
        name = Name.special("<anonymous>")
        this.returnType = returnType
        visibility = DescriptorVisibilities.LOCAL
        origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
    }.apply {
        val bodyBuilder = DeclarationIrBuilder(context, symbol, startOffset, endOffset)
        body = bodyBuilder.irBlockBody {
            block()
        }
        parent = lambdaParent
    }
    return IrFunctionExpressionImpl(startOffset, endOffset, lambdaType, lambda, IrStatementOrigin.LAMBDA)
}

fun IrBuilderWithScope.irCallCopy(
    overload: IrSimpleFunctionSymbol,
    original: IrCall,
    dispatchReceiver: IrExpression?,
    extensionReceiver: IrExpression?,
    valueArguments: List<IrExpression?>,
): IrExpression {
    return irCall(overload, type = original.type).apply {
        this.dispatchReceiver = original.dispatchReceiver?.deepCopyWithSymbols(parent)
        this.extensionReceiver = (extensionReceiver ?: original.extensionReceiver)?.deepCopyWithSymbols(parent)
        for (i in 0 until original.typeArgumentsCount) {
            putTypeArgument(i, original.getTypeArgument(i))
        }
        for ((i, argument) in valueArguments.withIndex()) {
            putValueArgument(i, argument?.deepCopyWithSymbols(parent))
        }
//        putValueArgument(valueArguments.size, messageArgument.deepCopyWithSymbols(parent))
    }
}
