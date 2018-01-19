/*
 * Copyright (C) 2018 Christopher J. Stehno <chris@stehno.com>
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

import groovy.transform.TypeChecked
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task used to check the dependencies of a project to ensure that there are no duplicate dependencies (with different
 * versions).
 *
 * If duplications are found - the build will fail.
 *
 * The task will accept the following inputs:
 *
 * <b>configurations</b> - a list of configuration names used to limit the dependency check. All configurations are checked if
 * this is not specified.
 */
@TypeChecked
class CheckDependenciesTask extends DefaultTask {

    // TODO: should I do (or allow) deep checking (like the other task)?

    @Input Collection<String> ignored = []
    @Input Collection<String> configurations = []
    @Input Class<? extends ResultListener> resultListenerClass = NoOpResultListener

    CheckDependenciesTask() {
        group = 'Verification'
        description = 'Checks the project dependencies for duplicate libraries with different versions.'
    }

    @TaskAction void checkDependencies() {
        DependencyCheckResults results = new DependencyCheckResults()
        ResultListener resultListener = resultListenerClass ? resultListenerClass.newInstance() : null

        Set<String> deps = [] as Set<String>

        (configurations ?: project.configurations.names).each { String cname ->
            project.configurations.getByName(cname).dependencies.each { d ->
                String key = "${d.group}:${d.name}"

                if (!ignored.contains(key) && !deps.add(key)) {
                    results[cname] = key
                    resultListener?.duplicated(cname, key)
                }
            }

            deps.clear()
        }

        if (results.hasDuplications()) {
            logger.error 'Dependency duplications detected ({}):', results.count()

            results.each { String cname, String groupModule ->
                logger.error '- Duplicated dependency in ({}) {}', cname, groupModule
            }

            throw new RuntimeException("Duplicate dependencies detected (${results.count()})")
        }
    }
}



