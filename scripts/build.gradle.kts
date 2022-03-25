plugins {
    id("multiplatform-library-convention")
    id("kotlinx-serialization")
}

dependencies {
    commonMainImplementation(libs.mokoWeb3)
    jvmMainImplementation(projects.sdk)
    commonMainImplementation(libs.kotlinxSerialization)
    commonMainImplementation(libs.kbignum)
}
