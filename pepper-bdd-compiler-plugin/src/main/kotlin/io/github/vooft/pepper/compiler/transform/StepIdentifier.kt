package io.github.vooft.pepper.compiler.transform

import java.util.UUID

data class StepIdentifier(val id: UUID, val prefix: StepPrefix, val name: String)

enum class StepPrefix {
    GIVEN,
    WHEN,
    THEN,
    AND
}
