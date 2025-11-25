package com.rocketzly.lintplugin.dependency

import com.rocketzly.lintplugin.LintHelper
import org.gradle.api.Project

/**
 * 添加依赖 - 兼容 AGP 7.0.4
 * Created by rocketzly on 2020/8/30.
 */
class DependencyHelper : LintHelper {

    companion object {
        const val DEPENDENCY_LINT_PATH = "io.github.gbfansheng:lint:1.0.3"
        private const val DEPENDENCY_LINT_INCREMENT_PATH_AGP7 = "io.github.gbfansheng:lintPatch:0.0.2"
        private const val DEPENDENCY_LINT_INCREMENT_PATH_AGP41 = "io.github.gbfansheng:lintPatch41:0.0.1"

        /**
         * 从 rootProject 的 buildscript classpath 中探测当前工程使用的 AGP 版本。
         */
        private fun detectAgpVersion(project: Project): String? {
            return try {
                val root = project.rootProject
                val classpathConfig = root.buildscript.configurations.findByName("classpath")
                    ?: return null
                val resolved = classpathConfig.resolvedConfiguration
                val agpDep = resolved.firstLevelModuleDependencies.firstOrNull {
                    it.moduleGroup == "com.android.tools.build" && it.moduleName == "gradle"
                }
                agpDep?.moduleVersion
            } catch (e: Exception) {
                project.logger.warn("无法检测 AGP 版本: ${e.message}")
                null
            }
        }

        /**
         * 注入lint补丁，目前包含增量扫描和bug修复功能
         */
        fun injectLintPatch(project: Project) {
            try {
                val agpVersion = (detectAgpVersion(project) ?: "").trim()

                if (agpVersion.isEmpty()) {
                    project.logger.lifecycle("未能检测到 AGP 版本，出于安全考虑暂不注入 lint 补丁")
                    return
                }

                val dependencyPath = when {
                    agpVersion.startsWith("4.1.") -> {
                        project.logger.lifecycle("检测到 AGP $agpVersion，注入 AGP 4.1.x 专用补丁: $DEPENDENCY_LINT_INCREMENT_PATH_AGP41")
                        DEPENDENCY_LINT_INCREMENT_PATH_AGP41
                    }
                    agpVersion.startsWith("7.") || agpVersion.startsWith("8.") -> {
                        // 默认走原有补丁逻辑（AGP 7.x 及以上）
                        project.logger.lifecycle("检测到 AGP $agpVersion，注入默认补丁: $DEPENDENCY_LINT_INCREMENT_PATH_AGP7")
                        DEPENDENCY_LINT_INCREMENT_PATH_AGP7
                    }
                    else -> {
                        project.logger.lifecycle("AGP 版本($agpVersion) 未显式支持，暂不注入 lint 补丁")
                        return
                    }
                }

                // 兼容不同 AGP/Gradle 版本的 lint 配置名称
                val configurationNames = project.configurations.names

                val targetConfiguration = when {
                    configurationNames.contains("lintClassPath") -> "lintClassPath"
                    configurationNames.contains("lintChecks") -> "lintChecks"
                    configurationNames.contains("lintCompile") -> "lintCompile"
                    else -> null
                }

                if (targetConfiguration == null) {
                    project.logger.lifecycle("未找到可用于注入补丁的 lint configuration，跳过注入: $dependencyPath")
                    return
                }

                project.dependencies.add(targetConfiguration, dependencyPath)
                project.logger.lifecycle("已通过 $targetConfiguration 注入 lint 补丁依赖: $dependencyPath")
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