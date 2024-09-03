package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.kotest.core.names.TestName
import io.kotest.core.source.sourceRef
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestScope
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

open class PepperSpec(block: suspend PepperSpecDsl.() -> Unit) : PepperSpecDsl, FunSpec() {
    init {
        addContainer(TestName("root"), false, null) {
            withContext(CurrentTestScope(this)) {
                block()
            }
        }
    }

    internal suspend fun <R> givenContainer(stepName: String, block: () -> R): R {
        return testContainer("Given", stepName, block)
    }

    internal suspend fun <R> whenContainer(stepName: String, block: () -> R): R {
        return testContainer("When", stepName, block)
    }

    internal suspend fun <R> thenContainer(stepName: String, block: () -> R): R {
        return testContainer("Then", stepName, block)
    }

    private suspend fun <R> testContainer(prefix: String, stepName: String, block: () -> R): R {
        println("$prefix: $stepName")

        val currentScope = coroutineContext[CurrentTestScope]!!.scope
        lateinit var capturedValue: CapturedValue<R>

        currentScope.registerTestCase(NestedTest(
            name = TestName("$prefix: ", stepName, true),
            disabled = false,
            config = null,
            type = Test,
            source = sourceRef()
        ) {
            capturedValue = CapturedValue(block())
        })

        return capturedValue.value
    }

    class CapturedValue<T>(val value: T)

}

data class CurrentTestScope(val scope: TestScope) : AbstractCoroutineContextElement(CurrentTestScope) {
    companion object Key : CoroutineContext.Key<CurrentTestScope>

    override fun toString(): String = "CurrentTestScope($scope)"
}

internal suspend fun <R> PepperSpecDsl.GivenContainer(stepName: String, block: () -> R): R =
    (this as PepperSpec).givenContainer(stepName, block)

internal suspend fun <R> PepperSpecDsl.WhenContainer(stepName: String, block: () -> R): R =
    (this as PepperSpec).whenContainer(stepName, block)

internal suspend fun <R> PepperSpecDsl.ThenContainer(stepName: String, block: () -> R): R =
    (this as PepperSpec).thenContainer(stepName, block)

@Target(AnnotationTarget.FUNCTION)
annotation class Step
