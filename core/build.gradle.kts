/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("publication-convention")
    kotlin("plugin.serialization")
//    id("dev.icerock.mobile.multiplatform.cocoapods")
    `maven-publish`
}

dependencies {
    jvmMainImplementation(libs.web3j)
    commonMainImplementation(libs.logback)

    commonMainApi(libs.ktorClient)
    commonMainApi(libs.ktorClientLogging)
    commonMainApi(libs.ktorClientJson)
    commonMainApi(libs.kotlinxSerialization)
    commonMainApi(libs.coroutines)
    commonMainApi(libs.kbignum)
    commonMainApi(libs.mokoWeb3)
    commonMainApi(libs.kotlinTest)
    commonMainApi(libs.kotlinTestAnnotations)
}

// cocoaPods {
//     pod("SwiftWeb3Wrapper")
// }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
