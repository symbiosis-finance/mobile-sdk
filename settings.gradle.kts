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
