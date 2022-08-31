package com.lhr.centre.processor

import com.lhr.centre.element.TableConstant
import javax.annotation.processing.ProcessingEnvironment

/**
 * @author lhr
 * @date 2021/9/1
 * @des
 */
internal object ElementFactory {

    /**
     * 生成组件路由表
     */
    fun generateElementRouterTableToJava(environment: ProcessingEnvironment, config: CentreElementConfig){
        val filer = environment.filer
        val tableClassName = config.elementName + "\$Table"
        val jfo = filer.createSourceFile(TableConstant.DEFAULT_PACKAGE + "." + tableClassName)

        val writer = jfo.openWriter()

        val javaCode = getJavaStringByElementConfig(TableConstant.DEFAULT_PACKAGE,tableClassName,config)

        writer.write(javaCode)
        writer.close()
    }

    /**
     * 生成java源代码
     */
    fun getJavaStringByElementConfig(packageName: String, className: String,config: CentreElementConfig): String{
        val sb = StringBuilder()
        //导入包命
        sb.append("package $packageName;")
        sb.append("\n")
        //编写java代码
        //class start
        sb.append("public final class $className implements ${CentreConstant.DEFAULT_INTERFACE} {")
            .append("\n")

        //method getTitle start
        sb.append("public String getTitle() {").append("\n")
        sb.append("return \"${config.elementName}\";").append("\n")
        //method getTitle end
        sb.append("}").append("\n")

        //method getExtra start
        sb.append("public String getExtra() {").append("\n")
        sb.append("return \"${getJsonStringByElementPluginConfig(config.extra)}\";").append("\n")
        //method getExtra end
        sb.append("}").append("\n")

        //class end
        sb.append("}")
        return sb.toString()
    }

    /**
     * 生成配置信息json类
     */
    fun getJsonStringByElementPluginConfig(extra: ArrayList<CentreElementPluginConfig>) : String {
        val jsonStringBuilder = StringBuilder()

        //json array start
        jsonStringBuilder.append("[")

        for (pluginConfig in extra) {
            //json object start
            jsonStringBuilder.append("{")

            jsonStringBuilder
                .append("\\\"${TableConstant.EXTRA_NAME}\\\"")
                .append(":")
                .append("\\\"${pluginConfig.name}\\\"")

            jsonStringBuilder.append(",")

            jsonStringBuilder
                .append("\\\"${TableConstant.EXTRA_VALUE}\\\"")
                .append(":")
                .append("\\\"${pluginConfig.value}\\\"")

            //json object end
            jsonStringBuilder.append("}")

            jsonStringBuilder.append(",")
        }

        //remove over comma
        jsonStringBuilder.deleteCharAt(jsonStringBuilder.length - 1)

        //json array end
        jsonStringBuilder.append("]")

        return jsonStringBuilder.toString()
    }
}