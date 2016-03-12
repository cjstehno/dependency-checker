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
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CheckDependenciesTaskTest {

    @Rule public TemporaryFolder projectDir = new TemporaryFolder()

    @Before void before() {
        TestResultListener.clear()
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
            resultListenerClass = 'com.stehno.gradle.depchecker.TestResultListener'
        }

        project.tasks.checkDependencies.execute()

        assert !TestResultListener.hasDuplicates()
    }

    @Test
    void 'checkDependencies: normal'() {
        Project project = ProjectBuilder.builder().withProjectDir(projectDir.newFolder()).build()

        project.apply plugin: 'java'
        project.apply plugin: DependencyCheckerPlugin

        project.repositories {
            jcenter()
        }

        project.dependencies {
        }

        project.tasks.checkDependencies.execute()

        assert !TestResultListener.hasDuplicates()
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
            resultListenerClass = 'com.stehno.gradle.depchecker.TestResultListener'
        }

        project.tasks.checkDependencies.execute()

        assert !TestResultListener.hasDuplicates()
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
            resultListenerClass = 'com.stehno.gradle.depchecker.TestResultListener'
        }

        try {
            project.tasks.checkDependencies.execute()
            Assert.fail()
        } catch (RuntimeException rex){
            // success
        }

        assert TestResultListener.hasDuplicates()
        assert TestResultListener.duplicatesFor('compile').size() == 1
        assert TestResultListener.duplicatesFor('compile').contains('commons-io:commons-io')
        assert TestResultListener.duplicatesFor('runtime').size() == 0
        assert TestResultListener.duplicatesFor('testCompile').size() == 1
        assert TestResultListener.duplicatesFor('testCompile').contains('junit:junit')
    }

    @Test @Ignore // FIXME: put this back when the check funx is back in
    void 'check depends on checkDependencies'() {
        Project project = ProjectBuilder.builder().withProjectDir(projectDir.newFolder()).build()

        project.apply plugin: 'java'
        project.apply plugin: DependencyCheckerPlugin

        assert project.tasks['check'].dependsOn.contains(project.tasks['checkDependencies'])
    }

    @Test
    void 'checkDependencies: with duplicates (filtered)'() {
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
            configurations = ['runtime', 'testCompile']
            resultListenerClass = 'com.stehno.gradle.depchecker.TestResultListener'
        }

        try {
            project.tasks.checkDependencies.execute()
            Assert.fail()
        } catch (RuntimeException rex){
            // success
        }

        assert TestResultListener.hasDuplicates()
        assert TestResultListener.duplicatesFor('compile').size() == 0
        assert TestResultListener.duplicatesFor('runtime').size() == 0
        assert TestResultListener.duplicatesFor('testCompile').size() == 1
        assert TestResultListener.duplicatesFor('testCompile').contains('junit:junit')
    }
}
