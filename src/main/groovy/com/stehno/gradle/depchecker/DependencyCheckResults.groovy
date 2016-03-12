package com.stehno.gradle.depchecker

import groovy.transform.TypeChecked

/**
 * Created by cjstehno on 3/12/16.
 */
@TypeChecked
class DependencyCheckResults {

    private final Map<String, List<String>> duplicates = [:]

    void putAt(String configName, String groupModule) {
        if (duplicates.containsKey(configName)) {
            duplicates[configName] << groupModule
        } else {
            duplicates[configName] = [groupModule]
        }
    }

    void each(Closure closure) {
        duplicates.each { String cname, List<String> values ->
            values.each { String val ->
                closure(cname, val)
            }
        }
    }

    boolean hasDuplications() {
        !duplicates.isEmpty()
    }

    int count() {
        duplicates.values().collect { it.size() }.sum() as int
    }
}