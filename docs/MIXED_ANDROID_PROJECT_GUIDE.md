
# Building one Android APK from a mixed-language project

This guide describes how a project containing Java, Kotlin, TypeScript/JavaScript, Python and C/C++ can converge into one APK. The root Android Gradle build is the orchestrator. Each non-JVM language needs a deliberate integration step.

## Suggested layout

MyApp/
  settings.gradle
  build.gradle
  gradle.properties
  app/
    build.gradle
    src/main/AndroidManifest.xml
    src/main/java/
    src/main/kotlin/
    src/main/python/
    src/main/cpp/
      CMakeLists.txt
      native-lib.cpp
    src/main/assets/web/
  web/
    package.json
    package-lock.json
    src/

## Root build.gradle (Groovy example)

plugins {
    id 'com.android.application' version '8.7.3' apply false
    id 'org.jetbrains.kotlin.android' version '2.3.0' apply false
    id 'com.chaquo.python' version '17.2.0' apply false
}

The settings plugin repositories should include:
- google()
- mavenCentral()
- gradlePluginPortal()

## app/build.gradle (illustrative integration)

plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.chaquo.python'
}

android {
    namespace 'com.example.mixedapp'
    compileSdk 35

    defaultConfig {
        applicationId 'com.example.mixedapp'
        minSdk 24
        targetSdk 35
        ndk { abiFilters 'arm64-v8a', 'x86_64' }
        externalNativeBuild {
            cmake { cppFlags '-std=c++17' }
        }
    }

    externalNativeBuild {
        cmake { path file('src/main/cpp/CMakeLists.txt') }
    }
}

chaquopy {
    defaultConfig {
        version = '3.12'
        // Add only packages which are actually required by the app.
        // pip { install 'requests' }
    }
}

This is a template, not a drop-in universal build file. Choose a Kotlin plugin version compatible with the project's Android Gradle Plugin, and add only the integrations the project uses.

## Java and Kotlin

Place Java sources in src/main/java and Kotlin sources in src/main/kotlin (or configure source sets). Gradle compiles both into the Android application module when the Android and Kotlin plugins are correctly configured.

## Python

Chaquopy integrates Python into an Android Gradle application. Its current documentation describes Python source placement under src/main/python, supported Python versions, ABI constraints and package installation. Check package compatibility before adding dependencies, since not every PyPI package provides Android-compatible wheels.

## C and C++

Use Android NDK with CMake or ndk-build. Configure Gradle externalNativeBuild, and export native functions through JNI when Java/Kotlin needs to call them. Build and test each desired Android ABI.

Example app/src/main/cpp/CMakeLists.txt:

cmake_minimum_required(VERSION 3.22.1)
project(mixed_native LANGUAGES C CXX)
add_library(mixed_native SHARED native-lib.cpp)
find_library(log-lib log)
target_link_libraries(mixed_native ${log-lib})

## TypeScript and JavaScript

Node.js is a build-time toolchain, not an Android APK runtime by itself. Use a defined framework or bundling strategy:

- React Native: use the React Native Gradle integration and its supported bundle tasks.
- WebView-based UI: run TypeScript/JavaScript bundling and copy output into src/main/assets/web before Android packaging.
- Other engine: follow that engine's Android Gradle integration.

Illustrative Gradle task for a WebView bundle (requires Node.js on PATH and a web/package-lock.json):

def webDir = rootProject.file('web')
def webDist = new File(webDir, 'dist')
def androidWebAssets = file('src/main/assets/web')

tasks.register('npmCi', Exec) {
    workingDir webDir
    commandLine 'npm', 'ci'
    inputs.files new File(webDir, 'package.json'), new File(webDir, 'package-lock.json')
}

tasks.register('bundleWeb', Exec) {
    dependsOn 'npmCi'
    workingDir webDir
    commandLine 'npm', 'run', 'build'
    inputs.dir new File(webDir, 'src')
    outputs.dir webDist
}

tasks.register('copyWebAssets', Copy) {
    dependsOn 'bundleWeb'
    from webDist
    into androidWebAssets
}

tasks.named('preBuild').configure {
    dependsOn 'copyWebAssets'
}

Adapt paths to the project's actual bundler output. Do not assume npm run build output is automatically included in the APK. Wire the task output into the Android source set or assets directory, and make the Gradle task a dependency of preBuild.

## Rust and Go

Cross-compile native libraries for Android ABIs, then package the .so files under the correct jniLibs/<abi> directories or configure a native build integration. Define a stable JNI/FFI boundary. A desktop Linux binary is not an Android library.

## What Luz Builder must eventually do

For a local build, Luz Builder needs to provision compatible JDK, Gradle, Android SDK/Build Tools, NDK/CMake, Node and optional language adapters, then run the project's Gradle task graph and stream its logs. The 0.3.0 project analyzer only identifies likely components; it does not yet execute these toolchains or build APK files.
