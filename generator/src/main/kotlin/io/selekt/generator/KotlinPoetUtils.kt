package io.selekt.generator

import com.squareup.kotlinpoet.*
import java.io.File

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class KotlinPoetDsl

private typealias FileSpecBuild = (@KotlinPoetDsl FileSpec.Builder).() -> Unit
private typealias TypeSpecBuild = (@KotlinPoetDsl TypeSpec.Builder).() -> Unit
private typealias PropSpecBuild = (@KotlinPoetDsl PropertySpec.Builder).() -> Unit
private typealias FunSpecBuild = (@KotlinPoetDsl FunSpec.Builder).() -> Unit
private typealias ParamSpecBuild = (@KotlinPoetDsl ParameterSpec.Builder).() -> Unit


fun file(className: ClassName, build: FileSpecBuild): FileSpec {
    return FileSpec.builder(className.packageName, className.simpleName).apply(build).build()
}

fun FileSpec.Builder.function(name: String, build: FunSpecBuild) {
    addFunction(FunSpec.builder(name).apply(build).build())
}

fun FileSpec.Builder.type(className: ClassName, build: TypeSpecBuild): TypeSpec {
    return TypeSpec.classBuilder(className).apply(build).build().also(this::addType)
}

fun TypeSpec.Builder.companionObject(build: TypeSpecBuild) {
    addType(TypeSpec.companionObjectBuilder().apply(build).build())
}

fun TypeSpec.Builder.function(name: String, build: FunSpecBuild) {
    addFunction(FunSpec.builder(name).apply(build).build())
}

fun TypeSpec.Builder.property(name: String, type: TypeName, vararg modifiers: KModifier, build: PropSpecBuild = {}): PropertySpec {
    return PropertySpec.builder(name, type, *modifiers).apply(build).build().also(this::addProperty)
}

fun TypeSpec.Builder.primaryConstructor(build: FunSpecBuild) {
    primaryConstructor(FunSpec.constructorBuilder().apply(build).build())
}

fun FunSpec.Builder.parameter(name: String, type: TypeName, vararg modifiers: KModifier, build: ParamSpecBuild = {}): ParameterSpec {
    return ParameterSpec.builder(name, type, *modifiers).apply(build).build().also(this::addParameter)
}

fun ClassName.receiverType() = LambdaTypeName.get(this, emptyList(), Unit::class.asTypeName())

fun FileSpec.write(generatedSourcesRoot: String) {
    val dir = File("$generatedSourcesRoot/${packageName.replace(".", File.separator)}")
    dir.mkdirs()
    File(dir, "$name.kt").writeText(toString())
}
