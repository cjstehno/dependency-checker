package com.stehno.gradle.depchecker

/**
 * Created by cjstehno on 3/12/16.
 */
interface ResultListener {

    void duplicated(String configName, String groupModule)
}