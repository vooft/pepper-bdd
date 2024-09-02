package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given

class PepperUnprocessedSpec : PepperSpec({
    Given
    `my test step`()
})

//class PepperProcessedSpec : PepperSpec({
//    GivenContainer { `my test step`() }
//})

fun main() {
    PepperUnprocessedSpec()
}
