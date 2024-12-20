package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When

class TwoScenariosSpec : PepperSpec({
    Scenario("first scenario", tags = listOf("Two scenarios")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, false)
    }

    Scenario("second scenario", tags = listOf("Two scenarios")) {
        Given
        val firstRandom = `generate random string`("first")

        When
        val secondRandom = `generate random string`("second")
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, false)
    }
})
