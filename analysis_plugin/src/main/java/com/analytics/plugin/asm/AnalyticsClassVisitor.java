package com.analytics.plugin.asm;

import com.analytics.plugin.AnalyticsConfig;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @CreateDate: 2022/7/28
 * @Author: mac
 * @Description: asm class 处理类
 */
public class AnalyticsClassVisitor extends ClassVisitor {
    private ClassVisitor mClassVisitor;
    private String[] mInterfaces;
    private String className;

    public AnalyticsClassVisitor(ClassVisitor classVisitor) {
        super(AnalyticsConfig.ASM_API, classVisitor);
        mClassVisitor = classVisitor;
    }



    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        super.visit(version, access, name, signature, superName, interfaces);
        mInterfaces = interfaces;
        className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        final MethodVisitor  methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        String nameDesc = name + descriptor;

        if (!AnalyticsClassModifier.isShouldModify(className, name)){
            return methodVisitor;
        }
        System.out.println("------------AnalyticsClassVisitor inject className=" + className + " name=" + name + " descriptor=" + descriptor + " nameDesc=" + nameDesc);

        return new AnalyticsDefaultMethodVisitor(AnalyticsConfig.ASM_API, methodVisitor, access, name, descriptor){
            @Override
            protected void onMethodEnter() {
                super.onMethodEnter();
                //方法前加入
                methodVisitor.visitLdcInsn(className);
                methodVisitor.visitLdcInsn(name);
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS,
                        AnalyticsConfig.ANALYTICS_METHOD_ENTER_HOOK,
                        AnalyticsConfig.ANALYTICS_ENTER_METHOD_DESCRIPTOR,
                        false);
            }

            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode);
                //方法后加入
                methodVisitor.visitLdcInsn(className);
                methodVisitor.visitLdcInsn(name);
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS,
                        AnalyticsConfig.ANALYTICS_METHOD_EXIT_HOOK,
                        AnalyticsConfig.ANALYTICS_EXIT_METHOD_DESCRIPTOR,
                        false);
            }
        };
    }
}
