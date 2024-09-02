package io.github.vooft.pepper.sample

import io.github.vooft.pepper.Step
import kotlin.random.Random

@Step
fun `my test step`(): String {
    println("my test step")
    return "hello " + Random.nextInt()
}

@Step
fun `my test step 2`(input: String): String {
    println("my test step 2, input: $input")
    return "world " + Random.nextInt()
}

@Step
fun `my test step 3`(input1: String, input2: String) {
    println("my test step 3, input1: $input1, input2: $input2")
}
