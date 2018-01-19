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