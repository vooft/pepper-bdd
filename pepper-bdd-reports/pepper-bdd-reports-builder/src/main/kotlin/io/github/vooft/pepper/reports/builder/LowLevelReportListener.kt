package io.github.vooft.pepper.reports.builder

import kotlin.coroutines.coroutineContext

interface LowLevelReportListener {
    suspend fun startScenario(className: String, name: String, tags: List<String>)
    suspend fun startStep(index: Int, prefix: String, name: String)
    suspend fun skipStep(index: Int, prefix: String, name: String)
    suspend fun addArgument(name: String, typeName: String, value: String)
    suspend fun finishStepWithError(error: Throwable)
    suspend fun finishStepWithSuccess(result: Any?)
    suspend fun finishScenario()

    companion object {
        suspend fun ifPresent(block: suspend LowLevelReportListener.() -> Unit) {
            coroutineContext[PepperReportListenerElement]?.listener?.block()
        }
    }
}
