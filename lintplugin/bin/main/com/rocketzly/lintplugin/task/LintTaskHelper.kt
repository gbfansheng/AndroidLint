package com.rocketzly.lintplugin.task

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.rocketzly.lintplugin.LintHelper
import org.gradle.api.Project

/**
 * User: Rocket
 * Date: 2020/9/4
 * Time: 11:39 AM
 * 简化版本的 LintTaskHelper，兼容 AGP 7.0.4
 */
class LintTaskHelper : LintHelper {

    override fun apply(project: Project) {
        project.afterEvaluate {
            // 简化实现，直接创建任务而不依赖内部 API
            if (project.plugins.hasPlugin(AppPlugin::class.java) || 
                project.plugins.hasPlugin(LibraryPlugin::class.java)) {
                
                // 创建全量 lint 任务
                project.tasks.register("lintFull", LintTask::class.java) { task ->
                    LintCreationAction.FullCreationAction(project, "debug").configure(task)
                }
                
                // 创建增量 lint 任务
                project.tasks.register("lintIncrement", LintTask::class.java) { task ->
                    LintCreationAction.IncrementCreationAction(project, "debug").configure(task)
                }
                
                project.logger.lifecycle("已注册自定义 Lint 任务: lintFull, lintIncrement")
            }
        }
    }
}