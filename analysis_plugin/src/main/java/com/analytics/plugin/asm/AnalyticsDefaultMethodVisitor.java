package com.analytics.plugin.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @CreateDate: 2022/7/28
 * @Author: mac
 * @Description: asm method visitor
 */
public class AnalyticsDefaultMethodVisitor extends AdviceAdapter {

    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param api           the ASM API version implemented by this visitor.
     * @param methodVisitor the method visitor to which this adapter delegates calls.
     * @param access        the method's access flags.
     * @param name          the method's name.
     * @param descriptor    the method's descriptor.
     */
    protected AnalyticsDefaultMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
    }
}
