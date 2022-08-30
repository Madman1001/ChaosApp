package com.analytics.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Pattern

class AnalyticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is privacy plugin PrivacyCheckPlugin in buildSrc"
        project.extensions.create('AnalysisExtension', AnalyticsExtension)
        project.afterEvaluate {
            println '配置信息======='
            AnalyticsExtension privacyExtension = project['AnalysisExtension']

            String proxyClass = privacyExtension.proxyClass
            if (proxyClass != null){
                AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS = proxyClass.replaceAll("\\.", "/")
            }

            Set<String> rulesIn = privacyExtension.rulesIn
            if (rulesIn != null && !rulesIn.isEmpty()) {
                AnalyticsConfig.rulesIn.clear()
                for (item in rulesIn) {
                    AnalyticsConfig.rulesIn.add(Pattern.compile(item))
                }
            }

            Set<String> rulesOut = privacyExtension.rulesOut
            if (rulesOut != null && !rulesOut.isEmpty()) {
                AnalyticsConfig.rulesOut.clear()
                for (item in rulesOut) {
                    AnalyticsConfig.rulesOut.add(Pattern.compile(item))
                }
            }

            if (AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS != null){
                String pc = AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS.replaceAll("/", ".")
                AnalyticsConfig.rulesOut.add(Pattern.compile("^${pc}\\..*"))
            }

            println '代理类 = ' + AnalyticsConfig.ANALYTICS_METHOD_HOOK_CLASS

            AnalyticsConfig.rulesIn.each {
                println '匹配规则 = ' + it.pattern()
            }

            AnalyticsConfig.rulesOut.each {
                println '排除规则 = ' + it.pattern()
            }

            AnalyticsConfig.ANALYTICS_INJECT = privacyExtension.isInjectCode
            println 'PrivacyConfig.isInject = ' + AnalyticsConfig.ANALYTICS_INJECT
        }
        project.android.registerTransform(new AnalyticsPluginTransform(project))
    }
}
