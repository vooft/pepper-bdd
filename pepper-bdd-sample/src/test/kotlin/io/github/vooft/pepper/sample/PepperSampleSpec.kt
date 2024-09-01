package io.github.vooft.pepper.sample

import io.github.vooft.pepper.Given
import io.github.vooft.pepper.PepperSpec

class PepperSampleSpec : PepperSpec({
    Given
    println("hello")
})

fun main() {
    PepperSampleSpec()
}
