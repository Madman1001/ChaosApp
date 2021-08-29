package com.lhr.centre.processor

import com.lhr.centre.annotation.CPlugin
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


/**
 * @author lhr
 * @date 2021/8/29
 * @des 注解处理类
 */
class CentreProcessor: AbstractProcessor() {
    private lateinit var environment: ProcessingEnvironment

    /**
     * 初始化
     */
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        environment = processingEnv
        val messager = processingEnv.messager
        messager.printMessage(Diagnostic.Kind.NOTE, "CentreProcessor init")
    }


    /**
     * 指定使用的Java版本
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    /**
     * 指定处理的注解类型
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val annoTypes = HashSet<String>()
        annoTypes.add(CPlugin::class.java.name)
        return annoTypes
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (roundEnv.processingOver()) {
            /*注解处理结束*/
            return true
        }

        /*筛选注解*/
        val elements = roundEnv.rootElements
        val plugins: MutableMap<CPlugin, String> = HashMap(4)
        if (elements.isNotEmpty()) {
            for (value in elements) {
                val plugin: CPlugin? = value.getAnnotation(CPlugin::class.java)
                environment.messager.printMessage(Diagnostic.Kind.NOTE, "-----------------------")
                if (plugin == null) {
                    continue
                }
                environment.messager.printMessage(Diagnostic.Kind.NOTE, plugin.toString())
                environment.messager.printMessage(Diagnostic.Kind.NOTE, value.toString())
                plugins[plugin] = value.toString()
            }
            /*处理注解*/

        }
        return true
    }

}