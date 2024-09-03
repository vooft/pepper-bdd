package io.github.vooft.pepper.sample

import io.github.vooft.pepper.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ForkJoinPool
import kotlin.random.Random

@Step
suspend fun `generate random string`(prefix: String): String {
    val random = Random.nextInt()
    delay(1)
    printlnWithThread("generating random number, random=$random")
    delay(1)
    return "$prefix $random"
}

data class CompareResult(val first: String, val second: String, val result: Boolean)

@Step
suspend fun `two strings are compared`(first: String, second: String): CompareResult {
    withContext(Dispatchers.IO) { printlnWithThread("comparing two strings, first=$first, second=$second") }
    delay(1)
    return CompareResult(first, second, first == second)
}

@Step
suspend fun `compare result is`(compareResult: CompareResult, expected: Boolean) {
    delay(1)
    if (compareResult.result != expected) {
        throw AssertionError("Expected $expected, but got ${compareResult.result}")
    } else {
        withContext(customDispatcher) { printlnWithThread("${compareResult.first} == ${compareResult.second} == $expected") }
    }
}

private val customDispatcher = ForkJoinPool.commonPool().asCoroutineDispatcher()
private fun printlnWithThread(message: String) {
    println("[${Thread.currentThread().name}] $message")
}