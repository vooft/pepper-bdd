package io.github.vooft.pepper.reports.builder

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class PepperReportListenerElement(
    val listener: LowLevelReportListener
) : AbstractCoroutineContextElement(PepperReportListenerElement) {
    companion object Key : CoroutineContext.Key<PepperReportListenerElement>
}
