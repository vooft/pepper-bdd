package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When

class PepperUnprocessedSpec : PepperSpec({
    Given
    val firstStepResult = `my test step`()

    When
    val secondStepResult = `my test step 2`(firstStepResult)

    Then
    `my test step 3`(firstStepResult, secondStepResult)
})

fun main() {
    PepperUnprocessedSpec()
}
