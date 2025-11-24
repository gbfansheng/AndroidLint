package com.rocketzly.lintplugin.dependency

import com.rocketzly.lintplugin.LintHelper
import org.gradle.api.Project

/**
 * 添加依赖 - 兼容 AGP 7.0.4
 * Created by rocketzly on 2020/8/30.
 */
class DependencyHelper : LintHelper {

    companion object {
        const val DEPENDENCY_LINT_PATH = "com.rocketzly:lint:1.0.3"
        const val DEPENDENCY_LINT_INCREMENT_PATH = "com.rocketzly:lintPatch:0.0.2"

        /**
         * 注入lint补丁，目前包含增量扫描和bug修复功能
         */
        fun injectLintPatch(project: Project) {
            try {
                // 简化实现，使用 lintCompile 配置
                project.dependencies.add("lintCompile", DEPENDENCY_LINT_INCREMENT_PATH)
                project.logger.lifecycle("已注入 lint 补丁依赖: $DEPENDENCY_LINT_INCREMENT_PATH")
            } catch (e: Exception) {
                project.logger.warn("无法注入 lint 补丁: ${e.message}")
            }
        }
    }

    override fun apply(project: Project) {
        try {
            project.dependencies.add("implementation", DEPENDENCY_LINT_PATH)
            project.logger.lifecycle("已添加 lint 依赖: $DEPENDENCY_LINT_PATH")
        } catch (e: Exception) {
            project.logger.warn("无法添加 lint 依赖: ${e.message}")
        }
    }
}