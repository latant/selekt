package io.selekt.generator

fun <K, V> map(build: MutableMap<K, V>.() -> Unit): Map<K, V> = mutableMapOf<K, V>().apply(build)