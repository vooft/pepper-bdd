package io.github.vooft.pepper.sample

fun a() {
    println("a")
}

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
