import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

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
    commonMainApi(projects.core)

    commonTestImplementation(libs.kotlinTestJUnit)

    jvmMainImplementation(libs.ktorClientCio)

    jvmTestImplementation(libs.kotlinTestJUnit)

//    iosImplementation(libs.kotlinTestJUnit)

//    iosArm64TestImplementation(libs.kotlinTestJUnit)
//    iosArm64TestImplementation(libs.ktorClientCio)
//    iosX64TestImplementation(libs.ktorClientCio)
//    iosX64TestImplementation(libs.kotlinTestJUnit)
//    iosTestImplementation(libs.ktorClientCio)
}

// kotlin {
//    val xcf = XCFramework("SymbiosisSDK")
//    ios {
//        binaries.framework {
//            export(projects.core)
//            export(libs.mokoWeb3)
//            baseName = "SymbiosisSDK"
//            xcf.add(this)
//        }
//    }
// }

// cocoaPods {
//     pod("SwiftWeb3Wrapper", onlyLink = true)
// }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
