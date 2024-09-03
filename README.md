![Build and test](https://github.com/vooft/pepper-bdd/actions/workflows/build.yml/badge.svg?branch=main)
![Releases](https://img.shields.io/github/v/release/vooft/pepper-bdd)
![Maven Central](https://img.shields.io/maven-central/v/io.github.vooft/pepper-bdd-core)
![License](https://img.shields.io/github/license/vooft/pepper-bdd)

# pepper-bdd
BDD library that automatically discovers "steps" in your test class and executes each one of them as a separate jUnit test.

This is implemented as a compiler plugin by modifying code of your test spec.

Based on [kotest](https://github.com/kotest/kotest)

# Example
```kotlin
class SimplePepperSpec : PepperSpec({
    Scenario("my test scenario") {
        Given
        val firstRandom = `generate random string`("first")
        val secondRandom = `generate random string`("second")

        When
        val compareResult = `two strings are compared`(firstRandom, secondRandom)

        Then
        `compare result is`(compareResult, false)
    }
})
```

<img src="docs/ordered-steps.png" height="200">
