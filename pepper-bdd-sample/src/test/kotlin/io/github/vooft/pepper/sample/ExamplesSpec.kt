package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When

class ExamplesSpec : PepperSpec({
    ScenarioExamples("multiply by two") {
        "example 1" { Example(input = 1, result = 2, compareResult = true) }
        "example 2" { Example(input = 2, result = 4, compareResult = true) }
        "example 3" { Example(input = 3, result = 5, compareResult = false) }
    } Outline {
        Given
        val multiplication = `multiply by two`(example.input)

        When
        val compareResult = `two ints are compared`(multiplication, example.result)

        Then
        `compare result is`(compareResult, example.compareResult)
    }
})

data class Example(val input: Int, val result: Int, val compareResult: Boolean)
