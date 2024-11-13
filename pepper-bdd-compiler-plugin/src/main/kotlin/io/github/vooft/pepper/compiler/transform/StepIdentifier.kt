package io.github.vooft.pepper.compiler.transform

data class StepIdentifier(val id: String, val prefix: StepPrefix, val name: String)

data class ScenarioIdentifier(val className: ClassName, val scenarioTitle: ScenarioTitle)

@JvmInline
value class ScenarioTitle(val title: String)

@JvmInline
value class ClassName(val name: String)

enum class StepPrefix {
    GIVEN,
    WHEN,
    THEN
}
