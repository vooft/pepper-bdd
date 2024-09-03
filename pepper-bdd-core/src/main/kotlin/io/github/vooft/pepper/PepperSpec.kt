package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl

open class PepperSpec(block: PepperSpecDsl.() -> Unit) : PepperSpecDsl {
    init {
        block()
    }

    internal fun <R> givenContainer(stepName: String, block: () -> R): R {
        println("Given: $stepName")
        return block()
    }

    internal fun <R> whenContainer(stepName: String, block: () -> R): R {
        println("When: $stepName")
        return block()
    }

    internal fun <R> thenContainer(stepName: String, block: () -> R): R {
        println("Then: $stepName")
        return block()
    }
}

fun <R> PepperSpecDsl.GivenContainer(stepName: String, block: () -> R): R = (this as PepperSpec).givenContainer(stepName, block)

internal fun <R> PepperSpecDsl.WhenContainer(stepName: String, block: () -> R): R = (this as PepperSpec).whenContainer(stepName, block)

internal fun <R> PepperSpecDsl.ThenContainer(stepName: String, block: () -> R): R = (this as PepperSpec).thenContainer(stepName, block)

@Target(AnnotationTarget.FUNCTION)
annotation class Step
