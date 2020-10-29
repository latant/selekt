package io.selekt

import java.util.*

@Suppress("UNCHECKED_CAST")
class GeneralObjectEncoder: GeneralEncoder<Any?>() {
    private val stack = Stack<Any?>()
    private fun push(value: Any?) { stack.push(value) }
    private fun pop() {
        if (stack.size == 1) return
        val value = stack.pop()
        when (val prev = stack.pop()) {
            is String -> (stack.peek() as MutableMap<String, Any?>)[prev] = value
            is MutableList<*> -> stack.push((prev as MutableList<Any?>).apply { add(value) })
        }
    }
    private fun value(value: Any?) {
        push(value)
        pop()
    }

    override val value get() = stack.peek()
    override fun startMap() = push(mutableMapOf<String, Any?>())
    override fun endMap() = pop()
    override fun startArray() = push(mutableListOf<Any?>())
    override fun endArray() = pop()
    override fun key(name: String) = push(name)
    override fun any(value: Any) = value(value)
    override fun nil() = value(null)
}