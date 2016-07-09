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

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.TypeChecked
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * FIXME: document
 */
@TypeChecked
class CheckAvailabilityTask extends DefaultTask {

    // FIXME: other task should allow deep checking (as here)
    // FIXME: is there an official way to get input from a project variable or cli property

    @Input Collection<String> configurations = []
    @Input Collection<String> repoUrls = []
    @Input Collection<String> ignored = []
    @Input boolean failOnMissing = false

    CheckAvailabilityTask() {
        name = 'checkAvailability'
        group = 'Verification'
        description = 'Checks the availability of the required dependencies against a specified artifact repository.'
    }

    @TaskAction void checkAvailability() {
        Collection<String> activeRepos = collectRepoUrls()
        if (activeRepos) {
            Set<DependencyCoordinate> coords = [] as Set<DependencyCoordinate>

            (configurations ?: project.configurations.names).each { String cname ->
                project.configurations.getByName(cname).resolvedConfiguration.firstLevelModuleDependencies.each { ResolvedDependency dep ->
                    collectDependencies(coords, dep)
                }
            }

            coords.findAll { !ignored.contains(it as String) }.each { c ->
                logger.debug "Checking availability of '$c'..."

                if (HttpHeadClient.exists(activeRepos, c)) {
                    logger.info "Availability check for ($c): PASSED"
                } else {
                    logger.error "Availability check for ($c): FAILED"
                    if (failOnMissing) {
                        // TODO: is this the best way to fail a build?
                        throw new RuntimeException('One or more dependencies were not resolvable from the configured repo urls.')
                    }
                }
            }

        } else {
            logger.info 'Availability check SKIPPED since there are no configured repo URLs.'
        }
    }

    private Collection<String> collectRepoUrls() {
        project.hasProperty('repoUrls') ? ((project.property('repoUrls') as String).split(',') as Collection<String>) : repoUrls
    }

    static void collectDependencies(final Set<DependencyCoordinate> found, final ResolvedDependency dep) {
        found << DependencyCoordinate.from(dep)
        dep.children.each { child ->
            collectDependencies(found, child)
        }
    }
}

@Immutable
class DependencyCoordinate {
    String group
    String name
    String version

    static DependencyCoordinate from(ResolvedDependency dep) {
        new DependencyCoordinate(dep.moduleGroup, dep.moduleName, dep.moduleVersion)
    }

    @Override
    String toString() { "$group:$name:$version" }

    String toPathSuffix() {
        "${group.replaceAll('\\.', '/')}/${name}/${version}/${name}-${version}.jar"
    }
}

@CompileStatic
class HttpHeadClient {

    static boolean exists(final Collection<String> baseUrls, final DependencyCoordinate coordinate) throws Exception {
        baseUrls.any { u ->
            check "${u}/${coordinate.toPathSuffix()}"
        }
    }

    private static boolean check(final String url) {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection()
        con.requestMethod = 'HEAD'
        con.responseCode == 200
    }
}