/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("javadoc-stub-convention")
    id("org.gradle.maven-publish")
    id("signing")
}

publishing {
    repositories {
        maven {
            name = "symbiosis-github"
            url = uri("https://maven.pkg.github.com/symbiosis-finance/mobile-sdk")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

