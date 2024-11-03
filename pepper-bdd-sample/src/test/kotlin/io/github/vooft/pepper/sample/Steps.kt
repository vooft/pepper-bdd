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

@Step
suspend fun `generate two random strings`(prefix: String): Pair<String, String> =
    `generate random string`(prefix) to `generate random string`(prefix)

data class CompareResult<T>(val first: T, val second: T, val result: Boolean)

@Step
suspend fun `two strings are compared`(first: String, second: String): CompareResult<String> {
    withContext(Dispatchers.IO) { printlnWithThread("comparing two strings, first=$first, second=$second") }
    delay(1)
    return CompareResult(first, second, first == second)
}

@Step
suspend fun <T> `compare result is '{expected}'`(compareResult: CompareResult<T>, expected: Boolean) {
    delay(1)
    if (compareResult.result != expected) {
        throw AssertionError("Expected $expected, but got ${compareResult.result}")
    } else {
        withContext(customDispatcher) { printlnWithThread("${compareResult.first} == ${compareResult.second} == $expected") }
    }
}

@Step
suspend fun `multiply {number} by two`(number: Int): Int {
    delay(1)
    return number * 2
}

@Step
suspend fun `two ints are compared`(first: Int, second: Int): CompareResult<Int> {
    withContext(Dispatchers.IO) { printlnWithThread("comparing two ints, first=$first, second=$second") }
    delay(1)
    return CompareResult(first, second, first == second)
}

@Step
fun `failing step`(): Unit = throw AssertionError("This step should fail")

private val customDispatcher = ForkJoinPool.commonPool().asCoroutineDispatcher()
private fun printlnWithThread(message: String) {
    println("[${Thread.currentThread().name}] $message")
}
