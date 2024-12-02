package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When
import io.kotest.core.annotation.Ignored
import kotlin.random.Random

@Ignored
class FlakyScenariosSpec : PepperSpec({
    Scenario("flaky scenario 1", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 2", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 3", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 4", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 5", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 6", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 7", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 8", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 9", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }

    Scenario("flaky scenario 10", tags = listOf("Flaky")) {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is '{expected}'`(compareResult, Random.nextBoolean())
    }
})
