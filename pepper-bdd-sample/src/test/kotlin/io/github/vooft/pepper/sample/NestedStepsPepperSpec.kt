package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given

class NestedStepsPepperSpec : PepperSpec({
    Scenario("Nested steps") {
        Given
        `generate two random strings`("both")
    }
})
