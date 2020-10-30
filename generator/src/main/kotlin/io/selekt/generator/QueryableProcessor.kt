package io.selekt.generator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import io.selekt.Exclude
import io.selekt.Queryable
import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.syntheticMethodForAnnotations
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types
import javax.tools.Diagnostic

lateinit var typeUtils: Types

@AutoService(Processor::class)
class QueryableProcessor : AbstractProcessor() {

    fun warn(msg: Any) = processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, msg.toString())
    fun error(msg: Any) = processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg.toString())

    override fun getSupportedOptions() = setOf("kapt.kotlin.generated")
    override fun getSupportedAnnotationTypes() = setOf(Queryable::class.qualifiedName)
    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_13

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        typeUtils = processingEnv.typeUtils
        val generatedSourcesRoot = processingEnv.options["kapt.kotlin.generated"]!!
        val elements = roundEnv.getElementsAnnotatedWith(Queryable::class.java).map { it as TypeElement }

        val builderModels = elements.map { e ->
            val packageName = processingEnv.elementUtils.getPackageOf(e).qualifiedName.toString()
            val enclosedElements = e.enclosedElements.associateBy { it.simpleName.toString() }
            val kotlinClass = e.kotlinClass()!!
            val publicProps = kotlinClass.properties
                .filter { Flag.IS_PUBLIC(it.flags) }
                .filter { enclosedElements[it.syntheticMethodForAnnotations?.name]?.hasAnnotation<Exclude>()?.not() ?: true }
                .map { it to (enclosedElements["get${it.name.capitalize()}"] as ExecutableElement) }
                .map { (kmp, ee) -> FieldModel(kmp.name, queryModel(KtType(kmp, ee))) }
            BuilderModel(
                className = ClassName(packageName, e.simpleName.toString()),
                fields = publicProps
            )
        }

        builderModels.forEach { generateBuilder(generatedSourcesRoot, it) }

        return false
    }

}