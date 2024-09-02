package io.github.vooft.pepper.sample

import io.github.vooft.pepper.Step
import kotlin.random.Random

@Step
fun `my test step`(): String {
    println("my test step")
    return "hello " + Random.nextInt()
}

@Step
fun `my test step 2`(input: String) {
    println("my test step 2, input: $input")
}
