package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.ScenarioDsl

internal suspend fun <R> ScenarioDsl.GivenContainer(stepName: String, block: suspend () -> R): R = testContainer("Given", stepName, block)

internal suspend fun <R> ScenarioDsl.WhenContainer(stepName: String, block: suspend () -> R): R = testContainer("When", stepName, block)

internal suspend fun <R> ScenarioDsl.ThenContainer(stepName: String, block: suspend () -> R): R = testContainer("Then", stepName, block)

internal suspend fun <R> ScenarioDsl.AndContainer(stepName: String, block: suspend () -> R): R = testContainer("And", stepName, block)
