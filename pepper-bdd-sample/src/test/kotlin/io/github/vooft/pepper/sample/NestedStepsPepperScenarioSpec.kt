package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperScenarioSpec
import io.github.vooft.pepper.dsl.Given

class NestedStepsPepperScenarioSpec :
    PepperScenarioSpec({
        Scenario("Nested steps") {
            Given
            `generate two random strings`("both")
        }
    })
