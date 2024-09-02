package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.Step
import io.github.vooft.pepper.dsl.Given

class PepperSampleSpec : PepperSpec({
    Given
    `my test step`()
})

@Step
fun `my test step`() {
    println("my test step")
}

fun main() {
    PepperSampleSpec()
}
