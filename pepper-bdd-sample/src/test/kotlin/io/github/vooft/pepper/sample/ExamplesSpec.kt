package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.PepperExample
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When

class ExamplesSpec : PepperSpec(tags = listOf("Examples"), {
    ScenarioExamples("multiply by two", tags = listOf("Two")) {
        "example 1" { Example(input = 1, result = 2, compareResult = true, tags = listOf("1")) }
        "example 2" { Example(input = 2, result = 4, compareResult = true, tags = listOf("Nested", "2")) }
        "example 3" { Example(input = 3, result = 5, compareResult = false, tags = listOf("Nested", "3")) }
    } Outline {
        Given
        val multiplication = `multiply {number} by two`(example.input)

        When
        val compareResult = `two ints are compared`(multiplication, example.result)

        Then
        `compare result is '{expected}'`(compareResult, example.compareResult)
    }

    ScenarioExamples("multiply by three", tags = listOf("Three")) {
        "example 1" { Example(input = 1, result = 3, compareResult = true, tags = listOf("1")) }
        "example 2" { Example(input = 2, result = 6, compareResult = true, tags = listOf("Nested", "2")) }
        "example 3" { Example(input = 3, result = 8, compareResult = false, tags = listOf("Nested", "3")) }
    } Outline {
        Given
        val multiplication = `multiply {number} by three`(example.input)

        When
        val compareResult = `two ints are compared`(multiplication, example.result)

        Then
        `compare result is '{expected}'`(compareResult, example.compareResult)
    }
})

data class Example(val input: Int, val result: Int, val compareResult: Boolean, override val tags: List<String>) : PepperExample
