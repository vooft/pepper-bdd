package io.github.vooft.pepper.compiler.transform

data class StepIdentifier(val id: String, val prefix: String, val name: String)

enum class StepPrefix {
    GIVEN,
    WHEN,
    THEN,
    AND
}
