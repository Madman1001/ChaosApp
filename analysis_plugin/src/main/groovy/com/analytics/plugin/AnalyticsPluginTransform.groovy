package com.analytics.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project
import com.analytics.plugin.asm.AnalyticsClassModifier
import org.apache.commons.io.FileUtils
import groovy.io.FileType
class AnalyticsPluginTransform extends Transform {

    private static final NAME = "AnalysisPluginTransformBuildSrc"

    private static final isIncremental = false

    Project project

    AnalyticsPluginTransform(project) {
        this.project = project
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return isIncremental
    }

    @Override
    boolean isCacheable() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws Exception {
        super.transform(transformInvocation)
        println "----------Privacy check transform start buildSrc----------"
        TransformOutputProvider outputProvider = transformInvocation.outputProvider

        //是否增量编译 isIncremental
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        Context context = transformInvocation.context

        //遍历class文件
        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                // 处理Jar
                processJarInput(context, jarInput, outputProvider)
            }

            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 处理源码文件
                processDirectoryInputs(context, directoryInput, outputProvider)
            }
        }

        println "----------Privacy check transform end buildSrc----------"
    }

    /**
     * 处理jar文件
     * @param jarInput
     * @param outputProvider
     */
    void processJarInput(Context context, JarInput jarInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.contentTypes, jarInput.scopes, Format.JAR)

        def modifiedJar = null
        if (AnalyticsConfig.ANALYTICS_INJECT) {
            modifiedJar = AnalyticsClassModifier.modifyJar(jarInput.file, context.getTemporaryDir())
        }
        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }

        println("== AnalyticsTransform jarInput = modifiedJar " + modifiedJar.name + "\ndest=" + dest)

        FileUtils.copyFile(modifiedJar, dest)
    }

    /**
     * 处理class文件
     * @param directoryInput
     * @param outputProvider
     */
    void processDirectoryInputs(Context context, DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        // 建立文件夹
        FileUtils.forceMkdir(dest)

        File dir = directoryInput.file

        if (dir) {
            Map<String, File> modifyMap = new HashMap<>()
            // 遍历以某一拓展名结尾的文件
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
                File modified = null
                if (AnalyticsConfig.ANALYTICS_INJECT) {
                    modified = AnalyticsClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir())
                }
                if (modified != null) {
                    // 包名 + 类名  /com/yxhuang/autotrack/android/app/MainActivity.class*/
                    String key = classFile.absolutePath.replace(dir.absolutePath, "")
                    modifyMap.put(key, modified)
                }
            }

            // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
            FileUtils.copyDirectory(directoryInput.file, dest)
            modifyMap.entrySet().each { Map.Entry<String, File> en ->
                File target = new File(dest.absolutePath + en.getKey())
                if (target.exists()) {
                    target.delete()
                }
                FileUtils.copyFile(en.getValue(), target)
                en.getValue().delete()
            }
        }
    }
}
