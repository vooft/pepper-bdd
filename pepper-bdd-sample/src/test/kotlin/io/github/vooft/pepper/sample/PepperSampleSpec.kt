package io.github.vooft.pepper.sample

import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(FUNCTION)
annotation class Test

@Test
fun testA() {
    println("a")
}

@Test
fun testB() {
    println("b")
}

class PepperSampleSpec {
    fun test() {
        testA()
        testB()
    }
}

fun main() {
    PepperSampleSpec().test()
}
