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

/**
 * Created by cjstehno on 3/12/16.
 */
class TestResultListener implements ResultListener {

    private static final Map<String, List<String>> duplicates = [:]

    static boolean hasDuplicates() {
        !duplicates.isEmpty()
    }

    static void clear() {
        duplicates.clear()
    }

    static List<String> duplicatesFor(String cname) {
        duplicates[cname] ? duplicates[cname].asImmutable() : []
    }

    @Override
    void duplicated(String configurationName, String groupModule) {
        if (duplicates.containsKey(configurationName)) {
            duplicates[configurationName] << groupModule
        } else {
            duplicates[configurationName] = [groupModule]
        }
    }
}
