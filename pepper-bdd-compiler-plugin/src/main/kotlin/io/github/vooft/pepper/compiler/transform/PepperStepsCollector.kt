package io.github.vooft.pepper.compiler.transform

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.util.UUID

internal class PepperStepsCollector(private val pluginContext: IrPluginContext) : IrElementTransformerVoid() {

    private var currentClassName: String? = null
    private val currentClassSteps = mutableListOf<StepIdentifier>()

    private val visitedClasses = mutableMapOf<String, List<StepIdentifier>>()
    val steps: Map<String, List<StepIdentifier>> get() {
        val current = currentClassName?.let { mapOf(it to currentClassSteps.toList()) } ?: mapOf()
        return (current + visitedClasses).filterValues { it.isNotEmpty() }
    }

    private val stepAnnotation = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("Step")
            )
        )
    )

    private val pepperSpecClass = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("PepperSpec")
            )
        )
    )

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val type = declaration.symbol.owner.returnType
        if (!type.isSubtypeOfClass(pepperSpecClass)) {
            return super.visitConstructor(declaration)
        }

        currentClassName?.let {
            visitedClasses[it] = currentClassSteps.toList()
            currentClassSteps.clear()
        }

        currentClassName = type.classFqName?.asString()
        return super.visitConstructor(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (currentClassName != null && expression.symbol.owner.hasAnnotation(stepAnnotation)) {
            currentClassSteps.add(StepIdentifier(UUID.randomUUID(), expression.symbol.owner.name.asString()))
        }

        return super.visitCall(expression)
    }
}
