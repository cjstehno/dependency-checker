/*
 * Copyright (C) 2017 Christopher J. Stehno <chris@stehno.com>
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

import static java.lang.Boolean.FALSE

/**
 * Gradle build task used to check configured dependencies against a set of external artifact repository URLs to determine which
 * are not available in at least one of the repositories.
 *
 * Allows searched configurations to be limited by specifying a "configurations" property with a collection of configuration names to be searched.
 *
 * Missing artifacts may be ignored by specifying their coordinate values in the "ignored" collection property. This will cause the specified artifacts
 * to be ignored during searching.
 *
 * By default, missing dependencies in the remote repo, will not fail the build; however, this may be changed by setting the "failOnMissing"
 * property to "true". All dependencies will be checked before the build is failed (if any are missing).
 */
@TypeChecked
class CheckAvailabilityTask extends DefaultTask {

    // FIXME: is there an official way to get input from a project variable or cli property

    @Input Collection<String> configurations = []
    @Input Collection<String> repoUrls = []
    @Input Collection<String> ignored = []
    @Input boolean failOnMissing = false

    CheckAvailabilityTask() {
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

            Map<Boolean, List<DependencyCoordinate>> results = coords.findAll { !ignored.contains(it as String) }.groupBy { c ->
                HttpHeadClient.exists(activeRepos, c)
            }

            results.each { Boolean passed, List<DependencyCoordinate> list ->
                list.each { c ->
                    if (passed) {
                        logger.info "Availability check for ($c): PASSED"
                    } else {
                        logger.error "Availability check for ($c): FAILED"
                    }
                }
            }

            if (results[FALSE] && failOnMissing) {
                // TODO: is this the best way to fail a build?
                throw new RuntimeException('One or more dependencies were not resolvable from the configured repo urls.')
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

/**
 * Very simple HTTP client for verifying the existence of a URL on a remote server (using HEAD requests).
 */
@CompileStatic
class HttpHeadClient {

    /**
     * Checks whether or not the given dependency coordinate exists on at least one of the specified repo URLs.
     *
     * @param baseUrls the base repo URLs
     * @param coordinate the dependency coordinate being tested
     * @return true if the dependency is found at one of the repo URLs, false if not.
     */
    static boolean exists(final Collection<String> baseUrls, final DependencyCoordinate coordinate) {
        baseUrls.any { u ->
            check("${u}/${coordinate.toPathSuffix()}")
        }
    }

    private static boolean check(final String url) {
        HttpURLConnection con = null
        try {
            con = (HttpURLConnection) new URL(url).openConnection()
            con.requestMethod = 'HEAD'
            con.responseCode == 200

        } catch (Exception ex) {
            // TODO: something better?
            ex.printStackTrace()
            false

        } finally {
            con?.disconnect()
        }
    }
}