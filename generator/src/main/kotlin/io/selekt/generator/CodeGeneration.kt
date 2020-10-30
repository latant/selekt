package io.selekt.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.selekt.Encoder
import io.selekt.MapQuery
import io.selekt.Query
import io.selekt.QueryDsl

fun ClassName.builderClassName() = ClassName(packageName, "${simpleName}QueryBuilder")

fun FunSpec.Builder.generateParameters(field: FieldModel, queryModel: QueryModel) {
    when (queryModel) {
        is QueryModel.Primitive -> {}
        is QueryModel.Wrapping -> generateParameters(field, queryModel.inner)
        is QueryModel.Queryable -> {
            parameter("query${field.name.capitalize()}", queryModel.queriedName.builderClassName().receiverType())
        }
    }
}

fun FunSpec.Builder.queryExpression(field: FieldModel, queryModel: QueryModel, outer: Pair<String, Array<Any>>): Pair<String, Array<Any>> {
    return when (queryModel) {
        is QueryModel.Primitive -> "%T${outer.first}" to arrayOf(queryModel.primitiveTypeName, *outer.second)
        is QueryModel.Wrapping -> queryExpression(field, queryModel.inner,
            "${outer.first}.${queryModel.wrapperTypeName.simpleName.removeSuffix("Query").toLowerCase()}()"
                to outer.second)
        is QueryModel.Queryable -> ("%T.build(query${field.name.capitalize()})${outer.first}"
            to arrayOf(queryModel.queriedName.builderClassName(), *outer.second))
    }
}

fun generateBuilder(generatedSourcesRoot: String, model: BuilderModel) {
    val className = model.className
    val builderClassName = model.className.builderClassName()
    val builderLambdaTypeName = builderClassName.receiverType()
    val queryTypeName = Query::class.asTypeName().parameterizedBy(className)
    val mapQueryTypeName = MapQuery::class.asTypeName().parameterizedBy(className)
    val classQueryName = "${className.simpleName.decapitalize()}Query"
    val queryClassName = "query${className.simpleName.capitalize()}"
    val encoderTypeVarName = TypeVariableName("E")

    file(builderClassName) {

        type(builderClassName) {
            addAnnotation(QueryDsl::class.asTypeName())
            primaryConstructor {
                addModifiers(PRIVATE)
            }
            val queriesProp = property("queries", MUTABLE_MAP.parameterizedBy(STRING, queryTypeName), PRIVATE) {
                initializer("mutableMapOf<String, %T>()", queryTypeName)
            }
            companionObject {
                function("build") {
                    val buildQuery = parameter("buildQuery", builderLambdaTypeName)
                    returns(mapQueryTypeName)
                    addStatement("return %T(%T().apply(%N).queries)", mapQueryTypeName, builderClassName, buildQuery.name)
                }
            }
            model.fields.forEach { field ->
                function(field.name) {
                    generateParameters(field, field.queryModel)
                    val (expr, args) = queryExpression(field, field.queryModel, "" to emptyArray())
                    addStatement("%N[%S] = $expr.of<%T> { it.${field.name} }", queriesProp, field.name, *args, className)
                }
            }
        }

        function(classQueryName) {
            val lambdaParam = parameter(queryClassName, builderLambdaTypeName)
            addStatement("return %T.build(%N)", builderClassName, lambdaParam)
        }

        function(queryClassName) {
            addTypeVariable(encoderTypeVarName)
            returns(encoderTypeVarName)
            val valueParam = parameter("value", className)
            val encoderParam = parameter("encoder", Encoder::class.asTypeName().parameterizedBy(encoderTypeVarName))
            val queryParam = parameter(queryClassName, queryTypeName)
            addStatement("%N.execute(%N, %N)", queryParam, encoderParam, valueParam)
            addStatement("return %N.value", encoderParam)
        }

        function(queryClassName) {
            addTypeVariable(encoderTypeVarName)
            returns(encoderTypeVarName)
            val valueParam = parameter("value", className)
            val encoderParam = parameter("encoder", Encoder::class.asTypeName().parameterizedBy(encoderTypeVarName))
            val buildQueryParam = parameter("query", builderLambdaTypeName)
            addStatement("%T.build(%N).execute(%N, %N)", builderClassName, buildQueryParam, encoderParam, valueParam)
            addStatement("return %N.value", encoderParam)
        }

        function("query") {
            receiver(className)
            addTypeVariable(encoderTypeVarName)
            returns(encoderTypeVarName)
            val encoderParam = parameter("encoder", Encoder::class.asTypeName().parameterizedBy(encoderTypeVarName))
            val queryParam = parameter(queryClassName, queryTypeName)
            addStatement("return %N(this, %N, %N)", queryClassName, encoderParam, queryParam)
        }

        function("query") {
            receiver(className)
            addTypeVariable(encoderTypeVarName)
            returns(encoderTypeVarName)
            val encoderParam = parameter("encoder", Encoder::class.asTypeName().parameterizedBy(encoderTypeVarName))
            val buildQueryParam = parameter("query", builderLambdaTypeName)
            addStatement("return %N(this, %N, %N)", queryClassName, encoderParam, buildQueryParam)
        }

    }.write(generatedSourcesRoot)
}
