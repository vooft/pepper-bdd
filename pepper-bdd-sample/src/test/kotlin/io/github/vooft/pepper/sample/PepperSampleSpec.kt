package io.github.vooft.pepper.sample

import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(FUNCTION)
annotation class Test

@Test
fun a() {
    println("a")
}

@Test
fun b() {
    println("b")
}

class PepperSampleSpec {
    fun test() {
        a()
        b()
    }
}

fun main() {
    PepperSampleSpec().test()
}
