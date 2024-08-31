package io.github.vooft.pepper.dsl

interface PepperSpecDsl

val PepperSpecDsl.Given: Unit get() = pepperFail()
val PepperSpecDsl.When: Unit get() = pepperFail()
val PepperSpecDsl.Then: Unit get() = pepperFail()

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
