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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockserver.client.server.MockServerClient
import org.mockserver.junit.MockServerRule
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.FAILED

class CheckAvailabilityTaskSpec extends Specification {

    @Rule public TemporaryFolder projectDir = new TemporaryFolder()
    @Rule public MockServerRule server = new MockServerRule(this)

    private MockServerClient client

    void 'checkAvailability --info (all available - fail on missing)'() {
        setup:
        def availables = available([
            'com.stehno.vanilla:vanilla-core:0.2.0', 'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207',
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46', 'junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3'
        ])

        saveBuildFile """
            checkAvailability {
                repoUrls = ['http://localhost:${server.port}/repo']
                failOnMissing = true
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability', '--info').build()

        then:
        checkForLoggedStatus result, availables, true
        checkForNoLoggedStatus result, false

        totalSuccess result
    }

    void 'checkAvailability --info (all available - fail on missing - override url)'() {
        setup:
        def availables = available([
            'com.stehno.vanilla:vanilla-core:0.2.0', 'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207',
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46', 'junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3'
        ])

        saveBuildFile """
            checkAvailability {
                repoUrls = ['http://foo.edu/repo']
                failOnMissing = true
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability', '--info', "-PrepoUrls=http://localhost:${server.port}/repo").build()

        then:
        checkForLoggedStatus result, availables, true
        checkForNoLoggedStatus result, false

        totalSuccess result
    }

    void 'checkAvailability --info (all available - fail on missing - runtime configuration)'() {
        setup:
        def availables = available([
            'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207',
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46'
        ])

        saveBuildFile """
            checkAvailability {
                repoUrls = ['http://localhost:${server.port}/repo']
                configurations = ['runtime']
                failOnMissing = true
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability', '--info').build()

        then:
        checkForLoggedStatus result, availables, true
        checkForNoLoggedStatus result, false

        totalSuccess result
    }

    void 'checkAvailability --info (some available - not fail on missing)'() {
        setup:
        def availables = available([
            'com.stehno.vanilla:vanilla-core:0.2.0', 'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207'
        ])

        def unavailables = unavailable([
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46', 'junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3'
        ])

        saveBuildFile """
            checkAvailability {
                repoUrls = ['http://localhost:${server.port}/repo']
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability', '--info').build()

        then:
        checkForLoggedStatus result, availables, true
        checkForLoggedStatus result, unavailables, false

        // The build succeeds since we are not failing on missing
        totalSuccess result
    }

    void 'checkAvailability (some available - not fail on missing - some ignored)'() {
        setup:
        def availables = available([
            'com.stehno.vanilla:vanilla-core:0.2.0', 'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207'
        ])

        def unavailables = unavailable([
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46', 'junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3'
        ])

        def ignoreds = ['junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3']

        saveBuildFile """
            checkAvailability {
                repoUrls = ['http://localhost:${server.port}/repo']
                ignored = ['junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3']
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability').build()

        then:
        checkForNoLoggedStatus result, true
        checkForLoggedStatus result, unavailables - ignoreds, false

        // The build succeeds since we are not failing on missing
        totalSuccess result
    }

    void 'checkAvailability --info (some available - fail on missing)'() {
        setup:
        available([
            'com.stehno.vanilla:vanilla-core:0.2.0', 'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207'
        ])

        unavailable([
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46', 'junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3'
        ])

        saveBuildFile """
            checkAvailability {
                repoUrls = ['http://localhost:${server.port}/repo']
                failOnMissing = true
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability', '--info').buildAndFail()

        then:
        result.tasks(FAILED).find { BuildTask task -> task.path == ':checkAvailability' }
    }

    void 'checkAvailability --info (some available - fail on missing - no repos)'() {
        setup:
        available([
            'com.stehno.vanilla:vanilla-core:0.2.0', 'commons-io:commons-io:2.4', 'org.crsh:crsh.shell.ssh:1.2.10', 'org.postgresql:postgresql:9.4.1207'
        ])

        unavailable([
            'org.crsh:crsh.shell.core:1.2.10', 'org.crsh:crsh.cli:1.2.10', 'org.codehaus.groovy:groovy-all:1.8.9', 'org.apache.sshd:sshd-core:0.6.0',
            'org.apache.mina:mina-core:2.0.4', 'org.slf4j:slf4j-api:1.6.1', 'org.apache.sshd:sshd-pam:0.6.0', 'net.sf.jpam:jpam:1.1',
            'commons-logging:commons-logging:1.0.4', 'org.bouncycastle:bcprov-jdk16:1.46', 'junit:junit:4.12', 'org.hamcrest:hamcrest-core:1.3'
        ])

        saveBuildFile """
            checkAvailability {
                failOnMissing = true
            }
        """

        when:
        BuildResult result = gradleRunner().withArguments('checkAvailability', '--info').build()

        then:
        checkForNoLoggedStatus result, true
        checkForNoLoggedStatus result, false
        totalSuccess result
    }

    private GradleRunner gradleRunner() {
        GradleRunner.create().withPluginClasspath().withProjectDir(projectDir.root)
    }

    private static boolean totalSuccess(final BuildResult result) {
        result.tasks.every { BuildTask task ->
            task.outcome == TaskOutcome.SUCCESS
        }
    }

    private static boolean checkForNoLoggedStatus(final BuildResult result, boolean found) {
        !result.output.contains(found ? 'PASSED' : 'FAILED')
    }

    private static boolean checkForLoggedStatus(final BuildResult result, final Collection<String> avails, boolean found) {
        avails.every { dep ->
            result.output.contains("Availability check for ($dep): ${found ? 'PASSED' : 'FAILED'}")
        }
    }

    private void saveBuildFile(final String taskConfig) {
        File buildFile = projectDir.newFile('build.gradle')
        buildFile.text = """
            plugins {
                id 'com.stehno.gradle.dependency-checker'
                id 'java'
            }

            repositories {
                jcenter()
            }

            dependencies {
                compile 'commons-io:commons-io:2.4'
                compile 'org.crsh:crsh.shell.ssh:1.2.10'

                runtime 'org.postgresql:postgresql:9.4.1207'

                testCompile 'junit:junit:4.12'
                testCompile('com.stehno.vanilla:vanilla-core:0.2.0') {
                    exclude group: 'org.codehaus.groovy', module: 'groovy-all'
                }
            }

            $taskConfig
        """.stripIndent()
    }

    private Collection<String> available(Collection<String> coords) {
        coords.each {
            prepareHeadRequest(it, 200)
        }
        coords
    }

    private Collection<String> unavailable(Collection<String> coords) {
        coords.each {
            prepareHeadRequest(it, 404)
        }
        coords
    }

    private static Map<String, String> coord(String value) {
        def parts = value.split(':')
        [group: parts[0], name: parts[1], version: parts[2]]
    }

    private void prepareHeadRequest(String coordinate, int code) {
        def dep = coord(coordinate)
        client.when(
            org.mockserver.model.HttpRequest.request().withMethod('HEAD').withPath("/repo/${dep.group.replaceAll('\\.', '/')}/$dep.name/$dep.version/$dep.name-${dep.version}.jar"),
            org.mockserver.matchers.Times.once()
        ).respond(org.mockserver.model.HttpResponse.response().withStatusCode(code))
    }
}
