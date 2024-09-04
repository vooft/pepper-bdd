package io.github.vooft.pepper.compiler.transform

data class StepIdentifier(val id: String, val prefix: StepPrefix, val name: String)

enum class StepPrefix {
    GIVEN,
    WHEN,
    THEN,
    AND
}
