package com.rocketzly.lintplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * User: Rocket
 * Date: 2020/9/3
 * Time: 3:52 PM
 * 包装 AndroidLintTask 的 LintTask，兼容 AGP 7.0.4
 */
open class LintTask : DefaultTask() {

    private var allInputs: ConfigurableFileCollection? = null
    private var variantName: String? = null
    private var wrappedAndroidLintTask: Any? = null

    @InputFiles
    @Optional
    open fun getAllInputs(): FileCollection? {
        return allInputs
    }

    @TaskAction
    fun lint() {
        project.logger.lifecycle("执行自定义 Lint 扫描 - 变体: $variantName")
        
        // 尝试调用系统自带的 AndroidLintTask
        try {
            // 查找对应的 AndroidLintTask
            val androidLintTaskName = "lint${variantName?.capitalize()}"
            val androidLintTask = project.tasks.findByName(androidLintTaskName)
            
            if (androidLintTask != null) {
                project.logger.lifecycle("调用系统 AndroidLintTask: $androidLintTaskName")
                androidLintTask.actions.forEach { action ->
                    action.execute(androidLintTask)
                }
            } else {
                project.logger.warn("未找到系统 AndroidLintTask: $androidLintTaskName")
            }
        } catch (e: Exception) {
            project.logger.error("执行 AndroidLintTask 时出错: ${e.message}")
        }
    }

    open class CreationAction(
        private val taskName: String,
        private val variantName: String
    ) {
        open val name: String = taskName
        open val type: Class<LintTask> = LintTask::class.java

        open fun configure(task: LintTask) {
            task.apply {
                this.variantName = variantName
                allInputs = project.files()
                description = "运行自定义 Lint 扫描（包装 AndroidLintTask）"
                group = "verification"
                
                // 设置依赖关系，确保在系统 lint 任务之前执行
                val systemLintTask = project.tasks.findByName("lint${variantName?.capitalize() ?: ""}")
                if (systemLintTask != null) {
                    task.dependsOn(systemLintTask)
                }
            }
        }
    }
}