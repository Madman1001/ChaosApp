package com.lhr.centre.processor

import com.lhr.centre.annotation.CElement
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
    /**
     * 注解环境对象
     */
    private lateinit var environment: ProcessingEnvironment

    /**
     * 组件配置信息
     */
    private lateinit var elementConfig: CentreElementConfig

    /**
     * 初始化
     */
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        environment = processingEnv
        val elementName = environment.options[CentreConstant.KEY_ELEMENT_NAME]?:"default"
        elementConfig = CentreElementConfig(elementName)

        val messager = processingEnv.messager
        messager.printMessage(Diagnostic.Kind.NOTE, "CentreProcessor init: $elementName")
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
        annoTypes.add(CElement::class.java.name)
        return annoTypes
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (roundEnv.processingOver()) {
            /*注解处理结束*/
            ElementFactory.generateElementRouterTableToJava(environment, elementConfig)
            return true
        }

        /*筛选注解*/
        val elements = roundEnv.rootElements
        if (elements.isNotEmpty()) {
            for (value in elements) {
                val plugin: CElement = value.getAnnotation(CElement::class.java) ?: continue

                environment.messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>>")
                environment.messager.printMessage(Diagnostic.Kind.NOTE, plugin.toString())
                environment.messager.printMessage(Diagnostic.Kind.NOTE, value.toString())
                environment.messager.printMessage(Diagnostic.Kind.NOTE, "<<<<<<<<<<<<<<<<<<<<<<")
                elementConfig.extra.add(value)
            }
            /*处理注解*/
        }
        return true
    }

}