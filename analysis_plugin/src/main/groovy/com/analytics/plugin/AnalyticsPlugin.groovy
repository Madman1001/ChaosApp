package com.analytics.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project

class AnalyticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is privacy plugin PrivacyCheckPlugin in buildSrc"
        project.extensions.create('AnalysisExtension', AnalyticsExtension)
        project.afterEvaluate {
            println '配置信息======='
            AnalyticsExtension privacyExtension = project['AnalysisExtension']

            Set<String> privacySet = privacyExtension.methodSet
            if (privacySet != null && !privacySet.isEmpty()) {
                for (item in privacySet) {
                    println '方法名=' + item
                }
                AnalyticsConfig.methodSet.clear()
                AnalyticsConfig.methodSet.addAll(privacySet)
            }

            Set<String> fieldSet = privacyExtension.fieldSet
            if (fieldSet != null && !fieldSet.isEmpty()) {
                for (item in fieldSet) {
                    println '属性名=' + item
                }
                AnalyticsConfig.fieldSet.clear()
                AnalyticsConfig.fieldSet.addAll(privacyExtension.fieldSet)
            }

            AnalyticsConfig.ANALYTICS_INJECT = privacyExtension.isInjectCode
            println 'PrivacyConfig.isInject = ' + AnalyticsConfig.ANALYTICS_INJECT

        }
        project.android.registerTransform(new AnalyticsPluginTransform(project))
    }
}
