package io.github.vooft.pepper

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.EXPRESSION

@Retention(SOURCE)
@Target(EXPRESSION)
annotation class Given

@Retention(SOURCE)
@Target(EXPRESSION)
annotation class When

@Retention(SOURCE)
@Target(EXPRESSION)
annotation class Then
