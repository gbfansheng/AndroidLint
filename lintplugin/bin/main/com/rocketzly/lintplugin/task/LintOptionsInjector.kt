package com.rocketzly.lintplugin.task

import com.android.build.gradle.internal.dsl.LintOptions
import com.rocketzly.lintplugin.extension.ExtensionHelper.Companion.EXTENSION_LINT_CONFIG
import com.rocketzly.lintplugin.extension.LintConfigExtension
import org.gradle.api.Project
import java.io.File

/**
 * 修改lintOption配置 - 兼容 AGP 7.0.4
 * Created by rocketzly on 2020/8/30.
 */
class LintOptionsInjector {

    companion object {
        val CHECK_LIST = setOf(
            "SerializableClassCheck",
            "HandleExceptionCheck",
            "AvoidUsageApiCheck",
            "DependencyApiCheck",
            "ResourceNameCheck"
        )
        const val XML_OUTPUT_RELATIVE_PATH = "build/reports/lint-results.xml"
        const val HTML_OUTPUT_RELATIVE_PATH = "build/reports/lint-results.html"
        const val BASELINE_RELATIVE_PATH = "lint-baseline.xml"

        fun inject(project: Project, lintOptions: LintOptions) {
            try {
                lintOptions.apply {
                    check.addAll(CHECK_LIST) //设置只检查的类型
                    xmlReport = true//启用 xml 报告
                    htmlReport = true//启用 html 报告
                    xmlOutput = File(project.projectDir, XML_OUTPUT_RELATIVE_PATH)//指定xml输出目录
                    htmlOutput = File(project.projectDir, HTML_OUTPUT_RELATIVE_PATH)//指定html输出目录
                    
                    // 在 AGP 7.0.4 中 warningsAsErrors 和 abortOnError 是私有的，无法设置
                    // 这些设置可能需要通过其他方式配置
                    
                    // 获取扩展配置
                    val lintConfig = project.extensions.findByName(EXTENSION_LINT_CONFIG) as? LintConfigExtension
                    if (lintConfig?.baseline == true) {
                        baseline(project.file(BASELINE_RELATIVE_PATH))//创建警告基准
                    }
                }
            } catch (e: Exception) {
                project.logger.warn("无法注入 LintOptions: ${e.message}")
            }
        }
    }
}