/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.github.jakemarsden.git-hooks") version "0.0.2"
}

buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.6.10")
        classpath(":symbiosis-sdk-build-logic")
    }
}

gitHooks {
    setHooks(mapOf("pre-commit" to "detekt"))
    setHooksDirectory(layout.projectDirectory.dir("../.git/modules/symbiosis-sdk/hooks"))
}

allprojects {
    plugins.withId("org.gradle.maven-publish") {
        group = "com.symbiosis"
        version = libs.versions.symbiosisSdkVersion.get()
    }
}
