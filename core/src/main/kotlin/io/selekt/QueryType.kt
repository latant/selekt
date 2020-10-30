package io.selekt

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class QueryType(vararg val classes: KClass<*>)