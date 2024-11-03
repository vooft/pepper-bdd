package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When
import io.kotest.core.annotation.Ignored

@Ignored
class FailingPepperSpec :
    PepperSpec({
        Scenario("failing scenario") {
            Given
            val firstRandom = `generate random string`("first")
            val secondRandom = `generate random string`("second")

            When
            val compareResult = `two strings are compared`(firstRandom, secondRandom)

            Then
            `compare result is '{expected}'`(compareResult, true)
        }
    })
