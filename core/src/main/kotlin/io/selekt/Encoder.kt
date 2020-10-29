package io.selekt

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.IntBuffer

interface Encoder<V> {
    fun startMap()
    fun endMap()
    fun startArray()
    fun endArray()
    fun key(name: String)

    fun boolean(value: Boolean)
    fun byte(value: Byte)
    fun short(value: Short)
    fun int(value: Int)
    fun long(value: Long)
    fun float(value: Float)
    fun double(value: Double)

    fun booleanArray(value: BooleanArray)
    fun byteArray(value: ByteArray)
    fun shortArray(value: ShortArray)
    fun intArray(value: IntArray)
    fun longArray(value: LongArray)
    fun floatArray(value: FloatArray)
    fun doubleArray(value: DoubleArray)

    fun byteBuffer(value: ByteBuffer)
    fun bigInteger(value: BigInteger)
    fun string(value: String)
    fun any(value: Any)
    fun nil()
    val value: V
}

inline fun Encoder<*>.array(action: Encoder<*>.() -> Unit) {
    startArray()
    action()
    endArray()
}

inline fun Encoder<*>.map(action: Encoder<*>.() -> Unit) {
    startMap()
    action()
    endMap()
}