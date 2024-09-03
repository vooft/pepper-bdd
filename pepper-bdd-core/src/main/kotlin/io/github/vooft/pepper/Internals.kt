package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.ScenarioDsl

internal suspend fun <R> ScenarioDsl.GivenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "Given", stepName, block)

internal suspend fun <R> ScenarioDsl.WhenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "When", stepName, block)

internal suspend fun <R> ScenarioDsl.ThenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "Then", stepName, block)

internal suspend fun <R> ScenarioDsl.AndContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "And", stepName, block)
