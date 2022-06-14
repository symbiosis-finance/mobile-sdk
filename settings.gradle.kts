/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
rootProject.name = "symbiosis-sdk"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven {
            url = uri("https://maven.pkg.github.com/symbiosis-finance/mobile-sdk")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
includeBuild("symbiosis-sdk-build-logic")

include(
    ":sdk",
    ":scripts",
    ":core"
)

//if (gradle.parent == null) {
//    include(":sample:android-app")
//    include(":sample:mpp-library")
//}
