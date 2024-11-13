package io.github.vooft.pepper.reports.builder

import kotlin.coroutines.coroutineContext

interface LowLevelReportListener {
    suspend fun startScenario(className: String, name: String)
    suspend fun startStep(prefix: String, name: String)
    suspend fun addArgument(name: String, typeName: String, value: String)
    suspend fun addError(error: Throwable)
    suspend fun addResult(result: Any?)
    suspend fun finishStep()
    suspend fun finishScenario()

    companion object {
        suspend fun ifPresent(block: suspend LowLevelReportListener.() -> Unit) {
            coroutineContext[PepperReportListenerElement]?.listener?.block()
        }
    }
}
