package io.selekt.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import io.selekt.AnyQuery
import io.selekt.NullableQuery
import io.selekt.Query
import io.selekt.QueryType
import io.selekt.Queryable
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

sealed class QueryModel {
    data class Primitive(val primitiveTypeName: ClassName) : QueryModel()
    data class Wrapping(val wrapperTypeName: ClassName, val inner: QueryModel) : QueryModel()
    data class Queryable(val queriedName: ClassName) : QueryModel()
}

data class FieldModel(val name: String, val queryModel: QueryModel)

data class BuilderModel(val className: ClassName, val fields: List<FieldModel>)



class KtType(val kmType: KmType, val typeMirror: TypeMirror, val isNullable: Boolean) {
    constructor(kmProperty: KmProperty, getter: ExecutableElement)
        : this(kmProperty.returnType, getter.returnType, getter.hasAnnotation<org.jetbrains.annotations.Nullable>())
    val className: String = (kmType.classifier as? KmClassifier.Class)?.name?.replace('/', '.')
        ?: error("Generics and aliases are not supported.")
    val argumentTypes get() = (kmType.arguments zip (typeMirror as DeclaredType).typeArguments)
        .map { (kmt, tm) -> kmt.type?.let { KtType(it, tm, false) } }
    val queryableAnnotation: Queryable? get() = (typeMirror as? DeclaredType)?.asElement()?.getAnnotation()
}

val models = map<String, (KtType) -> QueryModel> {
    Query::class.allSealedSubclasses.forEach { querySubclass ->
        val isPrimitive = querySubclass.objectInstance != null
        querySubclass.annotations.filterIsInstance<QueryType>().forEach { a ->
            a.classes.forEach { c ->
                put(c.qualifiedName!!, when {
                    isPrimitive -> ({ QueryModel.Primitive(querySubclass.asTypeName()) })
                    else -> ({
                        val innerModel = it.argumentTypes.first()?.let(::queryModel)
                            ?: QueryModel.Primitive(AnyQuery::class.asTypeName())
                        QueryModel.Wrapping(querySubclass.asTypeName(), innerModel)
                    })
                })
            }
        }
    }
}

fun queryModel(type: KtType): QueryModel {
    val model = models[type.className]?.invoke(type)
        ?: type.queryableAnnotation?.let { QueryModel.Queryable(ClassName.bestGuess(type.className)) }
        ?: QueryModel.Primitive(AnyQuery::class.asTypeName())
    return if (type.isNullable) QueryModel.Wrapping(NullableQuery::class.asTypeName(), model) else model
}