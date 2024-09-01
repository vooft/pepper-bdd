package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl

open class PepperSpec(block: PepperSpecDsl.() -> Unit) {
    init {
        val a = object : PepperSpecDsl {}
        a.block()
    }
}

val PepperSpecDsl.Given: Unit get() = pepperFail()
val PepperSpecDsl.When: Unit get() = pepperFail()
val PepperSpecDsl.Then: Unit get() = pepperFail()

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
