package io.selekt

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import kotlinx.metadata.Flag
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_13)
@SupportedAnnotationTypes("io.selekt.Queryable")
@SupportedOptions("kapt.kotlin.generated")
class QueryableProcessor : AbstractProcessor() {

    fun warn(msg: Any) = processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, msg.toString())
    fun error(msg: Any) = processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg.toString())

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val generatedSourcesRoot = processingEnv.options["kapt.kotlin.generated"]!!
        val elements = roundEnv.getElementsAnnotatedWith(Queryable::class.java).map { it as TypeElement }

        val builderModels = elements.map { e ->
            val enclosedElements = e.enclosedElements.associateBy { it.simpleName.toString() }
            val kotlinClass = e.kotlinClass()!!
            val publicGetters = kotlinClass.properties
                .filter { Flag.IS_PUBLIC(it.flags) }
                .associate { it.name to (enclosedElements["get${it.name.capitalize()}"] as ExecutableElement).returnType }
            val packageName = processingEnv.elementUtils.getPackageOf(e).qualifiedName.toString()
            BuilderModel(
                className = ClassName(packageName, e.simpleName.toString()),
                fields = publicGetters.map { FieldModel(it.key, queryModel(it.value)) }
            )
        }

        builderModels.forEach { generateBuilder(generatedSourcesRoot, it) }

        return false
    }

}