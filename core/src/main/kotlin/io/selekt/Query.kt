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
    fun nullable() = NullableQuery(this)
}

@QueryType(Boolean::class)
object BooleanQuery : ValueQuery<Boolean>() {
    override fun execute(encoder: Encoder<*>, value: Boolean) = encoder.boolean(value)
}

@QueryType(Byte::class)
object ByteQuery : ValueQuery<Byte>() {
    override fun execute(encoder: Encoder<*>, value: Byte) = encoder.byte(value)
}

@QueryType(Short::class)
object ShortQuery : ValueQuery<Short>() {
    override fun execute(encoder: Encoder<*>, value: Short) = encoder.short(value)
}

@QueryType(Int::class)
object IntQuery : ValueQuery<Int>() {
    override fun execute(encoder: Encoder<*>, value: Int) = encoder.int(value)
}

@QueryType(Long::class)
object LongQuery : ValueQuery<Long>() {
    override fun execute(encoder: Encoder<*>, value: Long) = encoder.long(value)
}

@QueryType(Float::class)
object FloatQuery : ValueQuery<Float>() {
    override fun execute(encoder: Encoder<*>, value: Float) = encoder.float(value)
}

@QueryType(Double::class)
object DoubleQuery : ValueQuery<Double>() {
    override fun execute(encoder: Encoder<*>, value: Double) = encoder.double(value)
}

@QueryType(BooleanArray::class)
object BooleanArrayQuery : ValueQuery<BooleanArray>() {
    override fun execute(encoder: Encoder<*>, value: BooleanArray) = encoder.booleanArray(value)
}

@QueryType(ByteArray::class)
object ByteArrayQuery : ValueQuery<ByteArray>() {
    override fun execute(encoder: Encoder<*>, value: ByteArray) = encoder.byteArray(value)
}

@QueryType(ShortArray::class)
object ShortArrayQuery : ValueQuery<ShortArray>() {
    override fun execute(encoder: Encoder<*>, value: ShortArray) = encoder.shortArray(value)
}

@QueryType(IntArray::class)
object IntArrayQuery : ValueQuery<IntArray>() {
    override fun execute(encoder: Encoder<*>, value: IntArray) = encoder.intArray(value)
}

@QueryType(LongArray::class)
object LongArrayQuery : ValueQuery<LongArray>() {
    override fun execute(encoder: Encoder<*>, value: LongArray) = encoder.longArray(value)
}

@QueryType(FloatArray::class)
object FloatArrayQuery : ValueQuery<FloatArray>() {
    override fun execute(encoder: Encoder<*>, value: FloatArray) = encoder.floatArray(value)
}

@QueryType(DoubleArray::class)
object DoubleArrayQuery : ValueQuery<DoubleArray>() {
    override fun execute(encoder: Encoder<*>, value: DoubleArray) = encoder.doubleArray(value)
}

@QueryType(BigInteger::class)
object BigIntegerQuery : ValueQuery<BigInteger>() {
    override fun execute(encoder: Encoder<*>, value: BigInteger) = encoder.bigInteger(value)
}

@QueryType(ByteBuffer::class)
object ByteBufferQuery : ValueQuery<ByteBuffer>() {
    override fun execute(encoder: Encoder<*>, value: ByteBuffer) = encoder.byteBuffer(value)
}

@QueryType(String::class)
object StringQuery : ValueQuery<String>() {
    override fun execute(encoder: Encoder<*>, value: String) = encoder.string(value)
}

@QueryType(List::class, Set::class, MutableList::class, MutableSet::class)
class IterableQuery<V>(private val query: Query<V>): ValueQuery<Iterable<V>>() {
    override fun execute(encoder: Encoder<*>, value: Iterable<V>) = encoder.array {
        value.forEach { query.execute(encoder, it) }
    }
    override fun toString() = "($query)"
}

@QueryType(Sequence::class)
class SequenceQuery<V>(private val query: Query<V>): ValueQuery<Sequence<V>>() {
    override fun execute(encoder: Encoder<*>, value: Sequence<V>) = encoder.array {
        value.forEach { query.execute(encoder, it) }
    }
    override fun toString() = "<$query>"
}

@QueryType(Array::class)
class ArrayQuery<V>(private val query: Query<V>): ValueQuery<Array<V>>() {
    override fun execute(encoder: Encoder<*>, value: Array<V>) = encoder.array {
        value.forEach { query.execute(encoder, it) }
    }
    override fun toString() = "[$query]"
}


object AnyQuery : ValueQuery<Any>() {
    override fun execute(encoder: Encoder<*>, value: Any) = encoder.any(value)
}

class GetQuery<V, U>(private val query: Query<V>, private val get: (U) -> V): Query<U>() {
    override fun execute(encoder: Encoder<*>, value: U) = query.execute(encoder, get(value))
    override fun toString() = query.toString()
}

class NullableQuery<V: Any>(private val query: ValueQuery<V>): Query<V?>() {
    override fun execute(encoder: Encoder<*>, value: V?) = value?.let { query.execute(encoder, it) } ?: encoder.nil()
    override fun toString() = "$query?"
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