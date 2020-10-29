package io.selekt

import java.math.BigInteger
import java.nio.ByteBuffer

sealed class Query<V> {
    abstract fun execute(encoder: Encoder<*>, value: V)
    fun iterable() = IterableQuery(this)
    fun sequence() = SequenceQuery(this)
    fun array() = ArrayQuery(this)
    fun <U> of(get: (U) -> V) = GetQuery(this, get)

    override fun toString() = javaClass.simpleName
}

sealed class ValueQuery<V: Any>: Query<V>() {
    fun nilable() = NilableQuery(this)
}

class GetQuery<V, U>(private val query: Query<V>, private val get: (U) -> V): Query<U>() {
    override fun execute(encoder: Encoder<*>, value: U) = query.execute(encoder, get(value))
    override fun toString() = query.toString()
}

class NilableQuery<V: Any>(private val query: ValueQuery<V>): Query<V?>() {
    override fun execute(encoder: Encoder<*>, value: V?) = value?.let { query.execute(encoder, it) } ?: encoder.nil()
    override fun toString() = "$query?"
}

class IterableQuery<V>(private val query: Query<V>): ValueQuery<Iterable<V>>() {
    override fun execute(encoder: Encoder<*>, value: Iterable<V>) = encoder.array {
        value.forEach { query.execute(encoder, it) }
    }
    override fun toString() = "($query)"
}

class SequenceQuery<V>(private val query: Query<V>): ValueQuery<Sequence<V>>() {
    override fun execute(encoder: Encoder<*>, value: Sequence<V>) = encoder.array {
        value.forEach { query.execute(encoder, it) }
    }
    override fun toString() = "<$query>"
}

class ArrayQuery<V>(private val query: Query<V>): ValueQuery<Array<V>>() {
    override fun execute(encoder: Encoder<*>, value: Array<V>) = encoder.array {
        value.forEach { query.execute(encoder, it) }
    }
    override fun toString() = "[$query]"
}

class MapQuery<V: Any>(private val queries: Map<String, Query<V>>): ValueQuery<V>() {
    override fun execute(encoder: Encoder<*>, value: V) = encoder.map {
        queries.forEach { (name, query) ->
            encoder.key(name)
            query.execute(encoder, value)
        }
    }
    override fun toString() = queries.toList().joinToString(", ", "{", "}") { "${it.first}: ${it.second}" }
}

object BooleanQuery : ValueQuery<Boolean>() {
    override fun execute(encoder: Encoder<*>, value: Boolean) = encoder.boolean(value)
}

object ByteQuery : ValueQuery<Byte>() {
    override fun execute(encoder: Encoder<*>, value: Byte) = encoder.byte(value)
}

object ShortQuery : ValueQuery<Short>() {
    override fun execute(encoder: Encoder<*>, value: Short) = encoder.short(value)
}

object IntQuery : ValueQuery<Int>() {
    override fun execute(encoder: Encoder<*>, value: Int) = encoder.int(value)
}

object LongQuery : ValueQuery<Long>() {
    override fun execute(encoder: Encoder<*>, value: Long) = encoder.long(value)
}

object FloatQuery : ValueQuery<Float>() {
    override fun execute(encoder: Encoder<*>, value: Float) = encoder.float(value)
}

object DoubleQuery : ValueQuery<Double>() {
    override fun execute(encoder: Encoder<*>, value: Double) = encoder.double(value)
}


object BooleanArrayQuery : ValueQuery<BooleanArray>() {
    override fun execute(encoder: Encoder<*>, value: BooleanArray) = encoder.booleanArray(value)
}

object ByteArrayQuery : ValueQuery<ByteArray>() {
    override fun execute(encoder: Encoder<*>, value: ByteArray) = encoder.byteArray(value)
}

object ShortArrayQuery : ValueQuery<ShortArray>() {
    override fun execute(encoder: Encoder<*>, value: ShortArray) = encoder.shortArray(value)
}

object IntArrayQuery : ValueQuery<IntArray>() {
    override fun execute(encoder: Encoder<*>, value: IntArray) = encoder.intArray(value)
}

object LongArrayQuery : ValueQuery<LongArray>() {
    override fun execute(encoder: Encoder<*>, value: LongArray) = encoder.longArray(value)
}

object FloatArrayQuery : ValueQuery<FloatArray>() {
    override fun execute(encoder: Encoder<*>, value: FloatArray) = encoder.floatArray(value)
}

object DoubleArrayQuery : ValueQuery<DoubleArray>() {
    override fun execute(encoder: Encoder<*>, value: DoubleArray) = encoder.doubleArray(value)
}


object BigIntegerQuery : ValueQuery<BigInteger>() {
    override fun execute(encoder: Encoder<*>, value: BigInteger) = encoder.bigInteger(value)
}

object ByteBufferQuery : ValueQuery<ByteBuffer>() {
    override fun execute(encoder: Encoder<*>, value: ByteBuffer) = encoder.byteBuffer(value)
}

object StringQuery : ValueQuery<String>() {
    override fun execute(encoder: Encoder<*>, value: String) = encoder.string(value)
}

object AnyQuery : ValueQuery<Any>() {
    override fun execute(encoder: Encoder<*>, value: Any) = encoder.any(value)
}
