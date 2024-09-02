package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl

open class PepperSpec(block: PepperSpecDsl.() -> Unit) {
    init {
        val a = object : PepperSpecDsl {}
        a.block()
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
