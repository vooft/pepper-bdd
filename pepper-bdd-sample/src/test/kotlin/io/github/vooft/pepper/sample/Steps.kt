package io.github.vooft.pepper.sample

import io.github.vooft.pepper.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.random.Random

@Step
suspend fun `my test step`(): String {
    val random = Random.nextInt()
    delay(1)
    println("[${Thread.currentThread().name}] my test step, random=$random")
    delay(1)
    return "hello $random"
}

@Step
suspend fun `my test step 2`(input: String): String {
    val random = Random.nextInt()
    delay(1)
    withContext(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] my test step 2, input: $input, random: $random")
    }
    delay(1)
    return "world $random"
}

@Step
suspend fun `my test step 3`(input1: String, input2: String) {
    delay(1)
    withContext(customDispatcher) {
        println("[${Thread.currentThread().name}] my test step 3, input1: $input1, input2: $input2")
    }
}

private val customDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
