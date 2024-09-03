package io.github.vooft.pepper.compiler.transform

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

fun IrPluginContext.findPepperSpec() = requireNotNull(
    referenceClass(
        ClassId(
            packageFqName = FqName("io.github.vooft.pepper"),
            topLevelName = Name.identifier("PepperSpec")
        )
    )
)

fun IrPluginContext.findPepperSpecDsl() = requireNotNull(
    referenceClass(
        ClassId(
            packageFqName = FqName("io.github.vooft.pepper.dsl"),
            topLevelName = Name.identifier("PepperSpecDsl")
        )
    )
)

fun IrPluginContext.findHelper(name: String) = run {
    referenceFunctions(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper.helper"),
            callableName = Name.identifier(name)
        )
    ).single().owner
}

fun IrBuilderWithScope.irLambda(
    returnType: IrType,
    lambdaType: IrType = context.irBuiltIns.functionN(0).typeWith(returnType),
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
        isSuspend = true
        isInline = true
    }.apply {
        val bodyBuilder = DeclarationIrBuilder(context, symbol, startOffset, endOffset)
        body = bodyBuilder.irBlockBody {
            block()
        }
        parent = lambdaParent
    }
    return IrFunctionExpressionImpl(startOffset, endOffset, lambdaType, lambda, IrStatementOrigin.LAMBDA)
}
