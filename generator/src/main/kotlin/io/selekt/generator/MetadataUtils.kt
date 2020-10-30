package io.selekt.generator

import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

fun Metadata.classHeader() = KotlinClassHeader(
    kind,
    metadataVersion,
    bytecodeVersion,
    data1,
    data2,
    extraString,
    packageName,
    extraInt
)
fun KotlinClassHeader.metadata() = KotlinClassMetadata.read(this)
fun TypeElement.kotlinClassMetadata() = getAnnotation(Metadata::class.java)?.classHeader()?.metadata()
fun TypeElement.kotlinClass() = (kotlinClassMetadata() as? KotlinClassMetadata.Class)?.toKmClass()

inline fun <reified A : Annotation> AnnotatedConstruct.getAnnotation(): A? = getAnnotation(A::class.java)
inline fun <reified A : Annotation> AnnotatedConstruct.hasAnnotation() = getAnnotation<A>() != null

val KClass<*>.allSealedSubclasses get(): List<KClass<*>> =
    sealedSubclasses.let { cs -> cs + cs.flatMap { it.allSealedSubclasses } }

