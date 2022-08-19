package com.lhr.learn.classcheck

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * @CreateDate: 2022/8/19
 * @Author: mac
 * @Description:
 */
class ClassBean(val clazz: Class<*>) {

    val superClassList: List<Class<*>>

    val interfaceClassList: List<Class<*>>

    val constructorsList: List<Constructor<*>>

    val fieldsList: List<Field>

    val methodsList: List<Method>

    init {
        val superList = mutableListOf<Class<*>>()
        var superclass = clazz.superclass
        while (superclass != null){
            superList.add(superclass)
            superclass = superclass.superclass
        }
        superClassList = superList

        val interfaceList = mutableListOf<Class<*>>()
        clazz.interfaces.forEach {
            interfaceList.add(it)
        }
        interfaceClassList = interfaceList

        val cList = mutableListOf<Constructor<*>>()
        clazz.declaredConstructors.forEach {
            cList.add(it)
        }
        constructorsList = cList

        val fList = mutableListOf<Field>()
        clazz.declaredFields.forEach {
            fList.add(it)
        }
        fieldsList = fList

        val mList = mutableListOf<Method>()
        clazz.declaredMethods.forEach {
            mList.add(it)
        }
        methodsList = mList
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(clazz.name).append("\n").append("\n")
        sb.append("ClassLoader:").append("\n")
        sb.append("\t").append(clazz.classLoader.javaClass.name).append("\n").append("\n")

        sb.append("Package:").append("\n")
        sb.append("\t").append(clazz.`package`?.name ?: "").append("\n")
        sb.append("\n").append("Super:").append("\n")
        var t_space = "\t"
        for (superClass in superClassList) {
            sb.append(t_space).append(superClass.name).append("\n")
            t_space += "\t"
        }

        sb.append("\n").append("Interface:").append("\n")
        for (classBean in interfaceClassList) {
            sb.append("\t").append(classBean.name).append("\n")
        }

        sb.append("\n").append("Constructor:").append("\n")
        for (constructor in constructorsList) {
            sb.append("\t").append(toStringConstructor(constructor)).append("\n")
        }

        sb.append("\n").append("Field:").append("\n")
        for (field in fieldsList) {
            sb.append("\t").append(toStringField(field)).append("\n")
        }

        sb.append("\n").append("Method:").append("\n")
        for (method in methodsList) {
            sb.append("\t").append(toStringMethod(method)).append("\n")
        }
        return sb.toString()
    }

    private fun toStringField(field: Field): String {
        val sb = StringBuilder()
        if (Modifier.isVolatile(field.modifiers)){
            sb.append("volatile ")
        }
        if (Modifier.isPrivate(field.modifiers)){
            sb.append("private ")
        } else if (Modifier.isProtected(field.modifiers)){
            sb.append("protected ")
        } else if (Modifier.isPublic(field.modifiers)){
            sb.append("public ")
        }

        if (Modifier.isFinal(field.modifiers)){
            sb.append("final ")
        }
        if (Modifier.isStatic(field.modifiers)){
            sb.append("static ")
        }

        sb.append(field.name)
        sb.append(": ")
        sb.append(field.type.name)
        return sb.toString()
    }

    private fun toStringMethod(method: Method): String {
        val sb = StringBuilder()
        if (Modifier.isSynchronized(method.modifiers)){
            sb.append("synchronized ")
        }
        if (Modifier.isPrivate(method.modifiers)){
            sb.append("private ")
        } else if (Modifier.isProtected(method.modifiers)){
            sb.append("protected ")
        } else if (Modifier.isPublic(method.modifiers)){
            sb.append("public ")
        }

        if (Modifier.isFinal(method.modifiers)){
            sb.append("final ")
        }
        if (Modifier.isStatic(method.modifiers)){
            sb.append("static ")
        }

        if (Modifier.isNative(method.modifiers)){
            sb.append("native ")
        }
        sb.append(method.name)
        sb.append(" (")
        if (method.parameterCount > 0){
            for (i in method.parameters.indices) {
                val clazz = method.parameters[i]
                sb.append(clazz.name)
                if (i != method.parameterCount - 1){
                    sb.append(", ")
                }
            }
        }
        sb.append(") ")
        sb.append(": ")
        sb.append(method.returnType.name)
        return sb.toString()
    }

    private fun toStringConstructor(constructor: Constructor<*>): String {
        val sb = StringBuilder()
        if (Modifier.isPrivate(constructor.modifiers)){
            sb.append("private ")
        } else if (Modifier.isProtected(constructor.modifiers)){
            sb.append("protected ")
        } else if (Modifier.isPublic(constructor.modifiers)){
            sb.append("public ")
        }

        sb.append(constructor.name)
        sb.append(" (")
        if (constructor.parameterCount > 0){
            for (i in constructor.parameters.indices) {
                val clazz = constructor.parameters[i]
                sb.append(clazz.name)
                if (i != constructor.parameterCount - 1){
                    sb.append(", ")
                }
            }
        }
        sb.append(") ")
        return sb.toString()
    }
}