package io.selekt

import java.math.BigInteger
import java.nio.ByteBuffer

abstract class GeneralEncoder<T> : Encoder<T> {
    override fun boolean(value: Boolean) = any(value)
    override fun byte(value: Byte) = any(value)
    override fun short(value: Short) = any(value)
    override fun int(value: Int) = any(value)
    override fun long(value: Long) = any(value)
    override fun float(value: Float) = any(value)
    override fun double(value: Double) = any(value)

    override fun booleanArray(value: BooleanArray) = any(value)
    override fun byteArray(value: ByteArray) = any(value)
    override fun shortArray(value: ShortArray) = any(value)
    override fun intArray(value: IntArray) = any(value)
    override fun longArray(value: LongArray) = any(value)
    override fun floatArray(value: FloatArray) = any(value)
    override fun doubleArray(value: DoubleArray) = any(value)

    override fun bigInteger(value: BigInteger) = any(value)
    override fun byteBuffer(value: ByteBuffer) = any(value)
    override fun string(value: String) = any(value)
}