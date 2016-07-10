# Gradle Dependency Checker Plugin

A Gradle plugin to assist in validating project dependencies.

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
    classpath "gradle.plugin.com.stehno.gradle:dependency-checker:0.2.1"
  }
}

apply plugin: "com.stehno.gradle.dependency-checker"
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:

```groovy
plugins {
  id "com.stehno.gradle.dependency-checker" version "0.2.1"
}
```

## Tasks

### CheckDependencies

A Task used to validate that the build has no duplicate dependencies - a dependency with the same group and name, but with a different version. The build
will be failed if any duplications are found.

#### Configuration

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

### CheckAvailability

Task which will validate your dependencies (deep search) against a specified collection of remote repositories to ensure that they are all available
at those repositories. This is useful in the scenario where your company has its own internal artifact repositiory and you need to determine whether
or not any need to be added to support your project.

#### Configuration

The following configuration properties are available:

##### `configurations`

Used to specify a collection of build configuration names to search for dependencies. If no list is provided, all configurations are searched.

##### `repoUrls`

Provides a collection of URL strings for your remote artifact repositories. These repositories must use standard maven directory layout. If no 
repos are provided, the task will silently pass. This property may also be provided on the command line so that the value need not be coded into the 
build file itself.

    ./gradlew checkAvailability -PrepoUrls=http://artifacts.mycompany.com/repository

##### `ignored`

You may provide a list of ignored artifact coordinates - these will not be checked against the remote repositories.

##### `failOnMissing`

By default, unavailable dependencies will be listed in the build output; however, if it is desirable to fail the build when artifacts are 
unavailable, this may be done by setting `failOnMissing = true`. The dependencies will be checked and a list of all failures will be provided before
the build is failed.
