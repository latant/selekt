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
    val moviesActed: List<Movie>,
    val moviesDirected: List<Movie>,
    val moviesProduced: List<Movie>,
    val followers: List<Person>,
    val peopleFollowed: List<Person>,
) {
    val movieTitles get() = moviesActed.map { it.title }
}

@Queryable
data class Movie(
    val title: String,
    val tagline: String,
    val released: Int,
    val director: Person,
    val writer: Person,
    val producer: Person,
    val actors: List<Person>,
)

val encoder = JacksonJsonEncoder(JsonFactory.builder().build())

fun Person.toDetailsJson() = query(encoder) {
    name()
    birth()
}

fun Person.toActorDetailsJson() = query(encoder) {
    name()
    birth()
    moviesActed {
        title()
    }
    peopleFollowed {
        name()
        birth()
    }
}

fun Movie.toDetailsJson() = query(encoder) {
    title()
    tagline()
    released()
    director {
        name()
        birth()
    }
    producer {
        name()
        birth()
    }
    actors {
        name()
        birth()
    }
}

fun Person.deeplyNestedData() = query(encoder) {
    name()
    birth()
    followers {
        name()
        peopleFollowed {
            name()
            moviesActed {
                actors {
                    moviesDirected {
                        title()
                    }
                }
            }
        }
    }
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

