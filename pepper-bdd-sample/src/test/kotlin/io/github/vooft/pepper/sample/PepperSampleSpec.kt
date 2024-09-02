package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given

class PepperUnprocessedSpec : PepperSpec({
    Given
    val var1 = `my test step`()
    `my test step 2`(var1)
})

//class PepperProcessedSpec : PepperSpec({
//    GivenContainer { `my test step`() }
//})

fun main() {
    PepperUnprocessedSpec()
}
