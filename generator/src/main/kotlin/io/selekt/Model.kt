package io.selekt

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeKind.*
import javax.lang.model.type.TypeKind.ARRAY
import javax.lang.model.type.TypeKind.BOOLEAN
import javax.lang.model.type.TypeKind.BYTE
import javax.lang.model.type.TypeKind.DOUBLE
import javax.lang.model.type.TypeKind.FLOAT
import javax.lang.model.type.TypeKind.INT
import javax.lang.model.type.TypeKind.LONG
import javax.lang.model.type.TypeKind.SHORT
import javax.lang.model.type.TypeMirror

sealed class QueryModel {
    data class Primitive(val primitiveTypeName: ClassName) : QueryModel()
    data class Wrapping(val wrapperTypeName: ClassName, val inner: QueryModel) : QueryModel()
    data class Queryable(val queriedName: ClassName) : QueryModel()
}

data class FieldModel(val name: String, val queryModel: QueryModel)

data class BuilderModel(val className: ClassName, val fields: List<FieldModel>)

private inline fun <reified Q: ValueQuery<*>> primitive() = QueryModel.Primitive(Q::class.asTypeName())
private inline fun <reified W: Query<*>> wrapping(model: QueryModel) = QueryModel.Wrapping(W::class.asTypeName(), model)
private inline fun <reified T> MutableMap<String, (DeclaredType) -> QueryModel?>.put(noinline create: (DeclaredType) -> QueryModel) {
    put(T::class.java.canonicalName, create)
}
private fun <K, V> Map<K, V>.default(value: V) = { key: K -> getOrDefault(key, value) }
private fun iterable(model: QueryModel) = wrapping<IterableQuery<*>>(model)
private fun sequence(model: QueryModel) = wrapping<SequenceQuery<*>>(model)
private fun array(model: QueryModel) = wrapping<ArrayQuery<*>>(model)

private val models = map<TypeKind, (TypeMirror) -> QueryModel?> {
    put(BOOLEAN) { primitive<BooleanQuery>() }
    put(BYTE) { primitive<ByteQuery>() }
    put(SHORT) { primitive<ShortQuery>() }
    put(INT) { primitive<IntQuery>() }
    put(LONG) { primitive<LongQuery>() }
    put(FLOAT) { primitive<FloatQuery>() }
    put(DOUBLE) { primitive<DoubleQuery>() }
    put(ARRAY) { t -> (t as ArrayType).componentType.let { arrayModels(it.kind)(it) } }
    put(DECLARED) { t -> (t as DeclaredType).let { declaredModels(it.asElement().toString())(it) }  }
}

private val arrayModels = map<TypeKind, (TypeMirror) -> QueryModel?> {
    put(BOOLEAN) { primitive<BooleanArrayQuery>() }
    put(BYTE) { primitive<ByteArrayQuery>() }
    put(SHORT) { primitive<ShortArrayQuery>() }
    put(INT) { primitive<IntArrayQuery>() }
    put(LONG) { primitive<LongArrayQuery>() }
    put(FLOAT) { primitive<FloatArrayQuery>() }
    put(DOUBLE) { primitive<DoubleArrayQuery>() }
}.default { array(queryModel(it)) }

val declaredModels = map<String, (DeclaredType) -> QueryModel?> {
    put<List<*>> { iterable(queryModel(it.typeArguments.first())) }
    put<Set<*>> { iterable(queryModel(it.typeArguments.first())) }
    put<Iterable<*>> { iterable(queryModel(it.typeArguments.first())) }
    put<Sequence<*>> { sequence(queryModel(it.typeArguments.first())) }
}.default { t ->
    t.asElement().getAnnotation(Queryable::class.java)
        ?.let { QueryModel.Queryable(ClassName.bestGuess(t.asElement().toString())) }
        ?: primitive<AnyQuery>()
}

fun queryModel(type: TypeMirror): QueryModel {
    val k = type.kind
    return models[type.kind]?.invoke(type) ?: error("Unsupported type: $type")
}