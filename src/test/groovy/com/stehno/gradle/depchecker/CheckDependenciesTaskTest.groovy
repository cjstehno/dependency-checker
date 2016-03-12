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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CheckDependenciesTaskTest {

    @Rule public TemporaryFolder projectDir = new TemporaryFolder()

    @Before void before() {
        InMemoryResultReporter.clear()
    }

    @Test
    void 'checkDependencies: no dependencies'() {
        Project project = ProjectBuilder.builder().withProjectDir(projectDir.newFolder()).build()

        project.apply plugin: 'java'
        project.apply plugin: DependencyCheckerPlugin

        project.repositories {
            jcenter()
        }

        project.dependencies {
        }

        project.checkDependencies {
            resultReporterClass = 'com.stehno.gradle.depchecker.InMemoryResultReporter'
        }

        project.tasks.checkDependencies.execute()

        assert !InMemoryResultReporter.hasDuplicates()
    }

    @Test
    void 'checkDependencies: without duplicates'() {
        Project project = ProjectBuilder.builder().withProjectDir(projectDir.newFolder()).build()

        project.apply plugin: 'java'
        project.apply plugin: DependencyCheckerPlugin

        project.repositories {
            jcenter()
        }

        project.dependencies {
            compile('com.stehno.vanilla:vanilla-core:0.2.0') {
                exclude group: 'org.codehaus.groovy', module: 'groovy-all'
            }
            compile 'commons-io:commons-io:2.4'

            runtime 'org.postgresql:postgresql:9.4.1207'

            testCompile 'junit:junit:4.12'
            testCompile('com.stehno.vanilla:vanilla-core:0.2.0') {
                exclude group: 'org.codehaus.groovy', module: 'groovy-all'
            }
        }

        project.checkDependencies {
            resultReporterClass = 'com.stehno.gradle.depchecker.InMemoryResultReporter'
        }

        project.tasks.checkDependencies.execute()

        assert !InMemoryResultReporter.hasDuplicates()
    }

    @Test
    void 'checkDependencies: with duplicates'() {
        Project project = ProjectBuilder.builder().withProjectDir(projectDir.newFolder()).build()

        project.apply plugin: 'java'
        project.apply plugin: DependencyCheckerPlugin

        project.repositories {
            jcenter()
        }

        project.dependencies {
            compile('com.stehno.vanilla:vanilla-core:0.2.0') {
                exclude group: 'org.codehaus.groovy', module: 'groovy-all'
            }
            compile 'commons-io:commons-io:2.4'
            compile 'commons-io:commons-io:2.3'

            runtime 'org.postgresql:postgresql:9.4.1207'

            testCompile 'junit:junit:4.12'
            testCompile 'junit:junit:4.10'
            testCompile('com.stehno.vanilla:vanilla-core:0.2.0') {
                exclude group: 'org.codehaus.groovy', module: 'groovy-all'
            }
        }

        project.checkDependencies {
            resultReporterClass = 'com.stehno.gradle.depchecker.InMemoryResultReporter'
        }

        project.tasks.checkDependencies.execute()

        assert InMemoryResultReporter.hasDuplicates()
        assert InMemoryResultReporter.duplicatesFor('compile').size() == 1
        assert InMemoryResultReporter.duplicatesFor('compile').contains('commons-io:commons-io')
        assert InMemoryResultReporter.duplicatesFor('runtime').size() == 0
        assert InMemoryResultReporter.duplicatesFor('testCompile').size() == 1
        assert InMemoryResultReporter.duplicatesFor('testCompile').contains('junit:junit')
    }
}
