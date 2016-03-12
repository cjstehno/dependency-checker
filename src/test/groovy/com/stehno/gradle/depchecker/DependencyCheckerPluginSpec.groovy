package com.stehno.gradle.depchecker

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DependencyCheckerPluginSpec extends Specification {

    def 'plugin'(){
        setup:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.stehno.gradle.dependency-check'

        expect:
//        project.tasks.checkDependencies instanceof DependencyCheckerPlugin
    }
}
