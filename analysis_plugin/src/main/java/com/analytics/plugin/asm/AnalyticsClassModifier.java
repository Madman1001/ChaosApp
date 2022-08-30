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

/**
 * @CreateDate: 2022/7/28
 * @Author: mac
 * @Description: asm class modifier
 */
public class AnalyticsClassModifier {

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
                JarEntry jarEntry2 = new JarEntry(entryName);
                jarOutputStream.putNextEntry(jarEntry2);

                byte[] modifiedClassBytes = null;
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                if (entryName.endsWith(".class")) {
                    modifiedClassBytes = modifyClass(sourceClassBytes);
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
     *
     * @param dir       -
     * @param classFile -
     * @param tempDir   -
     * @return -
     */
    public static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null;

        try {
            String classFileName = path2ClassName(classFile.getAbsolutePath().replace(dir.getAbsolutePath() + File.separator, ""));
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile));
            byte[] modifyClassBytes = modifyClass(sourceClassBytes);
            if (modifyClassBytes != null && modifyClassBytes.length > 0) {
                modified = new File(tempDir, classFileName.replace(".", "") + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                FileOutputStream out = new FileOutputStream(modified);
                out.write(modifyClassBytes);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            modified = classFile;
        }
        return modified;
    }

    public static byte[] modifyClass(byte[] sourceClass) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new AnalyticsClassVisitor(classWriter);
        ClassReader classReader = new ClassReader(sourceClass);
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }

    private static String path2ClassName(String pathName) {
        return pathName.replace(File.separator, ".").replace(".class", "");
    }

    /**
     * 过滤 .class 文件 增加编译速度
     *
     * @param className -
     * @return -
     */
    public static boolean isShouldModify(String className, String methodName) {
        boolean result = false;

        String targetName = className.replaceAll("/",".").replaceAll("\\\\",".");

        if (AnalyticsConfig.rulesIn.stream().anyMatch(pattern -> pattern.matcher(targetName + "." + methodName).matches())){
            result = true;
        }

        if (result){
            if (AnalyticsConfig.rulesOut.stream().anyMatch(pattern -> pattern.matcher(targetName + "." + methodName).matches())){
                result = false;
            }
        }

        System.out.println("-----AnalyticsTransform modifyClassFile = " + targetName + "." + methodName + " is should " + result);
        return result;
    }
}
