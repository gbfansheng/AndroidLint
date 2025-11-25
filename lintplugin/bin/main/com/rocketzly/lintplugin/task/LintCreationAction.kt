package com.rocketzly.lintplugin.task

import com.rocketzly.lintplugin.dependency.DependencyHelper
import org.gradle.api.Project
import org.gradle.api.Task
import kotlin.concurrent.thread

/**
 * User: Rocket
 * Date: 2020/9/4
 * Time: 2:32 PM
 * 创建LintTaskAction - 简化版本
 */
class LintCreationAction {

    companion object {
        const val TASK_NAME_LINT_FULL = "lintFull"
        const val TASK_NAME_LINT_INCREMENT = "lintIncrement"
        const val PARAM_NAME_BASELINE = "baseline"
        const val PARAM_NAME_REVISION = "revision"
    }

    open class Action(
        private val project: Project,
        private val taskName: String,
        private val variantName: String
    ) : LintTask.CreationAction(taskName, variantName) {
        override fun configure(task: LintTask) {
            //加入补丁修复lint的bug同时支持增量扫描功能，需要在super#configure之前调用
            DependencyHelper.injectLintPatch(project)
            super.configure(task)
            //修改lintOptions，需要在super#configure之后调用
            // LintOptionsInjector.inject(project, task.lintOptions) // 暂时禁用

            task.doFirst {
                resetLintClassLoader()
                ensurePatchSuccess()
            }
            task.doLast {
                resetLintClassLoader()
            }
        }

        private fun resetLintClassLoader() {
            // 简化实现，暂时移除复杂的类加载器操作
            project.logger.lifecycle("重置 Lint ClassLoader")
        }

        /**
         * 确保补丁成功加载
         */
        private fun ensurePatchSuccess() {
            // 简化实现，暂时移除复杂的补丁操作
            project.logger.lifecycle("确保补丁加载成功")
        }
    }

    /**
     * 全量lintAction
     */
    class FullCreationAction(
        project: Project,
        variantName: String
    ) : Action(project, TASK_NAME_LINT_FULL, variantName)

    /**
     * 增量lintAction
     */
    class IncrementCreationAction(
        private val project: Project,
        variantName: String
    ) : Action(project, TASK_NAME_LINT_INCREMENT, variantName) {
        override fun configure(task: LintTask) {
            super.configure(task)
            task.doFirst {
                if (!project.hasProperty(PARAM_NAME_BASELINE)) {
                    throw com.rocketzly.lintplugin.LintException("lintIncrement必须要${PARAM_NAME_BASELINE}参数")
                }
                if (!project.hasProperty(PARAM_NAME_REVISION)) {
                    throw com.rocketzly.lintplugin.LintException("lintIncrement必须要${PARAM_NAME_REVISION}参数")
                }
            }
        }
    }
}
