package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When

class PepperUnprocessedSpec : PepperSpec({
    Given
    val var1 = `my test step`()

    When
    val var2 = `my test step 2`(var1)

    Then
    `my test step 3`(var1, var2)
})

//class PepperProcessedSpec : PepperSpec({
//    GivenContainer { `my test step`() }
//})

fun main() {
    PepperUnprocessedSpec()
}
