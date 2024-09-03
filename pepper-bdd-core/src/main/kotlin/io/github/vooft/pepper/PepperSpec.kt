package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.Scenario
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.test.TestScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

open class PepperSpec(scenarioBlock: PepperSpecDsl.() -> Scenario) : FunSpec() {
    init {
        val dsl = PepperSpecDslImpl()
        val scenario = dsl.scenarioBlock()

        addContainer(TestName("Scenario: ${scenario.name}"), false, null) {
            withContext(CurrentTestScope(this)) { scenario.scenarioBody() }
        }
    }
}

data class CurrentTestScope(val scope: TestScope) : AbstractCoroutineContextElement(CurrentTestScope) {
    companion object Key : CoroutineContext.Key<CurrentTestScope>

    override fun toString(): String = "CurrentTestScope($scope)"
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
