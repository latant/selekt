package io.selekt.example

import com.fasterxml.jackson.core.JsonFactory
import io.selekt.*
import java.io.StringWriter
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.Charset

@Queryable
data class Person(
    val name: String,
    val birth: Int,
    val moviesActed: Sequence<Movie>,
    val followers: List<Person>,
    val a: List<List<Person>> = listOf(),
    val b: IntArray = IntArray(5),
    val c: Array<Person> = emptyArray(),
)

@Queryable
data class Movie(
    val title: String,
    val tagline: String,
    val released: Int,
    val directors: List<Person>,
    val actors: List<Person>
)

fun main() {
    val q = personQuery {
        name()
        birth()
        moviesActed {
            title()
            tagline()
            directors {
                name()
                moviesActed {
                    title()
                }
            }
        }
    }
    val p = Person("Anti", 1997, sequenceOf(Movie("Goal", "legjobb", 2000, listOf(), listOf())), listOf())
    val e = GeneralObjectEncoder()
    val j = JacksonJsonEncoder(JsonFactory.builder().build())
    println(p.query(j) {
        name()
        birth()
        moviesActed {
            title()
            tagline()
            directors {
                name()
                moviesActed {
                    title()
                }
            }
        }
    })
    println(p.query(e) {
        name()
        birth()
        moviesActed {
            title()
            tagline()
            directors {
                name()
                moviesActed {
                    title()
                }
            }
        }
    })
}

class JacksonJsonEncoder(private val factory: JsonFactory) : Encoder<String> {
    private val writer = StringWriter()
    private val generator = factory.createGenerator(writer)

    override val value: String get() {
        generator.flush()
        return writer.toString()
    }
    override fun boolean(value: Boolean) = generator.writeBoolean(value)
    override fun byte(value: Byte) = generator.writeNumber(value.toInt())
    override fun short(value: Short) = generator.writeNumber(value)
    override fun int(value: Int) = generator.writeNumber(value)
    override fun long(value: Long) = generator.writeNumber(value)
    override fun float(value: Float) = generator.writeNumber(value)
    override fun double(value: Double) = generator.writeNumber(value)

    override fun byteArray(value: ByteArray) = string(value.toString(Charset.defaultCharset()))
    override fun booleanArray(value: BooleanArray) = array { value.forEach(generator::writeBoolean) }
    override fun shortArray(value: ShortArray) = array { value.forEach(generator::writeNumber) }
    override fun intArray(value: IntArray) = array { value.forEach(generator::writeNumber) }
    override fun longArray(value: LongArray) = array { value.forEach(generator::writeNumber) }
    override fun floatArray(value: FloatArray) = array { value.forEach(generator::writeNumber) }
    override fun doubleArray(value: DoubleArray) = array { value.forEach(generator::writeNumber) }

    override fun bigInteger(value: BigInteger) = generator.writeNumber(value)
    override fun byteBuffer(value: ByteBuffer) = byteArray(value.array())
    override fun string(value: String) = generator.writeString(value)
    override fun startMap() = generator.writeStartObject()
    override fun endMap() = generator.writeEndObject()
    override fun startArray() = generator.writeStartArray()
    override fun endArray() = generator.writeEndArray()
    override fun key(name: String) = generator.writeFieldName(name)
    override fun any(value: Any) = generator.writeObject(value)
    override fun nil() = generator.writeNull()

}

