package io.github.vooft.pepper.sample

import io.github.vooft.pepper.PepperSpec
import io.github.vooft.pepper.dsl.Given
import io.github.vooft.pepper.dsl.Then
import io.github.vooft.pepper.dsl.When

class ExamplesSpec : PepperSpec({
    ScenarioExamples("multiply by two") {
        "example 1" { Example(input = 1, multiplication = 2, compareResult = true) }
        "example 2" { Example(input = 2, multiplication = 4, compareResult = true) }
        "example 3" { Example(input = 3, multiplication = 5, compareResult = false) }
    } Outline {
        Given
        val multiplication = `multiply by two`(example.input)

        When
        val compareResult = `two ints are compared`(multiplication, example.multiplication)

        Then
        `compare result is`(compareResult, true)
    }
})

data class Example(val input: Int, val multiplication: Int, val compareResult: Boolean)
