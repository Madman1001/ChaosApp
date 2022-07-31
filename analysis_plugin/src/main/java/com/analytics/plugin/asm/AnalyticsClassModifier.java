package com.analytics.plugin.asm;

import com.analytics.plugin.AnalyticsConfig;
import com.android.annotations.Nullable;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;

/**
 * @CreateDate: 2022/7/28
 * @Author: mac
 * @Description: asm class modifier
 */
public class AnalyticsClassModifier{

    @Nullable
    public static File modifyJar(File jarFile, File tempDir) throws IOException {
        // 读取原 jar
        JarFile file = new JarFile(jarFile, false);

        // 设置输出的 jar
        File outputJar = new File(tempDir, jarFile.getName());
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
        Enumeration<JarEntry> enumeration = file.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
            InputStream inputStream = null;
            try {
                inputStream = file.getInputStream(jarEntry);
            } catch (Exception e) {
                System.out.println(" \n--- modifyJar Exception " + e.getMessage());
                e.printStackTrace();
                return null;
            }
            String entryName = jarEntry.getName();
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className;
                JarEntry jarEntry2 = new JarEntry(entryName);
                jarOutputStream.putNextEntry(jarEntry2);

                byte[] modifiedClassBytes = null;
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                if (entryName.endsWith(".class")) {
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "");
                    if (isShouldModify(className)){
                        modifiedClassBytes = modifyClass(sourceClassBytes);
                    }
                }
                if (modifiedClassBytes == null) {
                    modifiedClassBytes = sourceClassBytes;
                }
                jarOutputStream.write(modifiedClassBytes);
                jarOutputStream.closeEntry();
            }
        }
        jarOutputStream.close();
        file.close();
        return outputJar;
    }

    /**
     * 对 class 文件进行修改
     * @param dir -
     * @param classFile -
     * @param tempDir -
     * @return -
     */
    public static File modifyClassFile(File dir, File classFile, File tempDir){
        String className = classFile.getAbsolutePath().replace(File.separator,"/");
        if (!isShouldModify(className)) {
            return classFile;
        }

        File modified = null;

        try {
            String classFileName = path2ClassName(classFile.getAbsolutePath().replace(dir.getAbsolutePath() + File.separator, ""));
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile));
            byte[] modifyClassBytes = modifyClass(sourceClassBytes);
            if (modifyClassBytes != null && modifyClassBytes.length > 0){
                modified = new File(tempDir, classFileName.replace(".", "") + ".class");
                if (modified.exists()){
                    modified.delete();
                }
                modified.createNewFile();
                new FileOutputStream(modified).write(modifyClassBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            modified = classFile;
        }
        return modified;
    }

    public static byte[] modifyClass(byte[] sourceClass){
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new AnalyticsClassVisitor(classWriter);
        ClassReader classReader = new ClassReader(sourceClass);
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }

    private static String path2ClassName(String pathName){
        return pathName.replace(File.separator, ".").replace(".class", "");
    }

    /**
     *  过滤 .class 文件 增加编译速度
     * @param className -
     * @return -
     */
    public static boolean isShouldModify(String className) {
        boolean result = true;
        if (className.contains(AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS) ||
                className.contains("R$") ||
                className.contains("R2$") ||
                className.contains("R2.class") ||
                className.contains("BuildConfig.class")) {
            result = false;
        }
        System.out.println("-----AnalyticsTransform modifyClassFile = " + className + " is should " + result);
        return result;
    }
}
