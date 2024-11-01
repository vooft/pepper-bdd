package io.github.vooft.pepper.compiler.transform

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class PepperReferences(pluginContext: IrPluginContext) {
    val pepperScenarioSpec = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("PepperScenarioSpec")
            )
        )
    )

    val pepperScenarioDsl = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper.dsl"),
                topLevelName = Name.identifier(PEPPER_SCENARIO_DSL)
            )
        )
    )

    val stepAnnotation = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper"),
                topLevelName = Name.identifier("Step")
            )
        )
    )

    val stepContainer = pluginContext.referenceFunctions(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper.helper"),
            callableName = Name.identifier("StepContainer")
        )
    ).single().owner

    val scenarioDsl = requireNotNull(
        pluginContext.referenceClass(
            ClassId(
                packageFqName = FqName("io.github.vooft.pepper.dsl"),
                topLevelName = Name.identifier("ScenarioDsl")
            )
        )
    )

    val prefixGiven = pluginContext.findStep("Given")
    val prefixWhen = pluginContext.findStep("When")
    val prefixThen = pluginContext.findStep("Then")

    val addStep = pluginContext.findHelper("addStep")

    companion object {
        val pepperScenarioDslFqName get() = FqName("io.github.vooft.pepper.dsl.$PEPPER_SCENARIO_DSL")
        val scenarioDslFqName get() = FqName("io.github.vooft.pepper.dsl.ScenarioDsl")
    }
}

private fun IrPluginContext.findStep(name: String) = requireNotNull(
    referenceProperties(
        callableId = CallableId(
            packageName = FqName("io.github.vooft.pepper.dsl"),
            callableName = Name.identifier(name)
        )
    ).single().owner.getter
).symbol

private const val PEPPER_SCENARIO_DSL = "PepperScenarioDsl"
