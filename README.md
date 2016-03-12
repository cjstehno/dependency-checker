# Gradle Dependency Checker Plugin

A Gradle plugin used to validate that the build has no duplicate dependencies - a dependency with the same group and name, but with a different version.

The plugin provides one task, the `checkDependencies` task, which will validate the dependencies by configuration and fail the build if any duplicates are found.

## Install 

Build script snippet for use in all Gradle versions:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.stehno.gradle:dependency-checker:0.1.3"
  }
}

apply plugin: "com.stehno.gradle.dependency-checker"
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:

```groovy
plugins {
  id "com.stehno.gradle.dependency-checker" version "0.1.3"
}
```

## Configuration

Without any configuration changes, the `checkDependencies` task will check all available configurations for duplicate dependencies and fail the build 
if it finds any. It will also report all duplicated dependencies in the standard output.

You can limit the scope of the dependency checking to a list of specified configurations using the task configuration, as follows:

```groovy
checkDependencies {
    configurations = ['compile','runtime']
}
```

The above code snippet, when added to your `build.gradle` will limit the dependency checking to the `compile` and `runtime` configurations, all others 
will be ignored.

You can easily make your build automatically check the dependencies by adding one or both of the following lines to your `build.gradle` file:

    tasks.check.dependsOn checkDependencies
    
    tasks.build.dependsOn checkDependencies
    
This will make `check` and `build` dependent on the `checkDependencies` task so that it will always be run. This is the safest way to integrate
this plugin into your build, since generally it will be forgotten and not run so that duplication will creep back into your build.