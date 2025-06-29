package io.github.vooft.pepper.compiler.transform

import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

fun List<ScopeWithIr>.findScenarioDslBlock(references: PepperReferences) = reversed().firstOrNull {
    val element = it.irElement as? IrSimpleFunction ?: return@firstOrNull false
    val extension = element.pepperExtensionReceiverParameter ?: return@firstOrNull false
    element.name.asString() == "<anonymous>" && extension.type.isSubtypeOfClass(references.scenarioDslSymbol)
}?.irElement as? IrSimpleFunction

fun IrCall.findScenarioTitle(): String? {
    if (symbol.owner.name.asString() in listOf("Scenario", "ScenarioExamples") &&
        dispatchReceiver?.type?.classFqName == PepperReferences.pepperSpecDslFqName
    ) {
        val titleConst = arguments[0] as? IrConst ?: return null
        return titleConst.value as? String
    }

    return null
}

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

fun IrPluginContext.findStepAnnotation() = requireNotNull(
    referenceClass(
        ClassId(
            packageFqName = FqName("io.github.vooft.pepper"),
            topLevelName = Name.identifier("Step")
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

val IrFunction.pepperExtensionReceiverParameter: IrValueParameter? get() = parameters.firstOrNull { it.kind == IrParameterKind.ExtensionReceiver }
var IrFunctionAccessExpression.pepperExtensionReceiver: IrExpression?
    get() = arguments[symbol.owner.parameters.indexOfFirst { it.kind == IrParameterKind.ExtensionReceiver }]
    set(value) {
        arguments[symbol.owner.parameters.indexOfFirst { it.kind == IrParameterKind.ExtensionReceiver }] = value
    }
val IrFunction.pepperValueParameters: List<IrValueParameter> get() = parameters.filter { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
