package com.stehno.gradle.depchecker

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by cjstehno on 3/12/16.
 */
class DependencyCheckerPlugin implements Plugin<Project>{
    /*
    https://docs.gradle.org/current/userguide/custom_plugins.html

    TODO: how to make this part of the "check" lifecycle
    TODO: allow limit to configuration
    TODO: pull out task object
    */

    @Override
    void apply(Project project) {
        project.task('checkDependencies') << {
            println "================"
            project.configurations.names.each { String cname->
                println "- $cname"
                Set<String> deps = [] as Set<String>
                project.configurations.getByName(cname).dependencies.each { d->
                    String key = "${d.group}:${d.name}"
                    if(!deps.add(key)){
                        println "Found duplicate: ${d.group}:${d.name}"
                    }
                }
            }
            println "================"
        }
    }
}
