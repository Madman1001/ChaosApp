package com.analytics.plugin;

import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class AnalyticsConfig {
    public static int ASM_API = Opcodes.ASM9;
    public static boolean ANALYTICS_INJECT = false;
    public static String ANALYTICS_METHOD_HOOK_CLASS = "com/lhr/chaos/LogHelper";
    public static String ANALYTICS_METHOD_ENTER_HOOK = "onMethodEnter";
    public static String ANALYTICS_METHOD_EXIT_HOOK = "onMethodExit";
    public static String ANALYTICS_ENTER_METHOD_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    public static String ANALYTICS_EXIT_METHOD_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";

    public static Set<Pattern> rulesIn = new HashSet<>();

    public static Set<Pattern> rulesOut = new HashSet<>();
}
