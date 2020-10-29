package io.selekt

import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.TypeElement

fun Metadata.classHeader() = KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
fun KotlinClassHeader.metadata() = KotlinClassMetadata.read(this)
fun TypeElement.kotlinClassMetadata() = getAnnotation(Metadata::class.java)?.classHeader()?.metadata()
fun TypeElement.kotlinClass() = (kotlinClassMetadata() as? KotlinClassMetadata.Class)?.toKmClass()