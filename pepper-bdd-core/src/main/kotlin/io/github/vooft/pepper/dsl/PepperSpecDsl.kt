package io.github.vooft.pepper.dsl

interface PepperSpecDsl

fun <R> GivenContainer(stepName: String, block: () -> R): R {
    println("Given: $stepName")
    return block()
}

val PepperSpecDsl.Given: Unit get() = pepperFail()
val PepperSpecDsl.When: Unit get() = pepperFail()
val PepperSpecDsl.Then: Unit get() = pepperFail()

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
