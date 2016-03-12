/*
 * Copyright (C) 2016 Christopher J. Stehno <chris@stehno.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.gradle.depchecker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME

/**
 * Gradle plugin providing a means to verify that a project does not have multiple versions of a dependency library
 * configured.
 */
class DependencyCheckerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Task checkDepTask = project.task 'checkDependencies', type: CheckDependenciesTask

        // make the dependency check part of the overall check
        project.getTasksByName(CHECK_TASK_NAME, true)?.each { Task t ->
            t.dependsOn checkDepTask
        }
    }
}
