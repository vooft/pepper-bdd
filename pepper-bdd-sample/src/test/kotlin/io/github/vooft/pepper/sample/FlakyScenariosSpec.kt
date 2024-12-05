package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When
import io.kotest.core.annotation.Ignored
import kotlin.random.Random

@Ignored
class FlakyScenariosSpec : PepperSpec(tags = listOf("Flaky"), {
    Scenario("flaky scenario 1") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 2") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 3") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 4") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 5") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 6") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 7") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 8") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 9") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 10") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }
})
