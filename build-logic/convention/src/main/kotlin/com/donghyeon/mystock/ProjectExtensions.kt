package com.donghyeon.mystock

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

const val COMPILE_SDK = 37
const val TARGET_SDK = 37
const val MIN_SDK = 28

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.android: CommonExtension
    get() = extensions.getByName("android") as CommonExtension

fun CommonExtension.configureAndroid() {
    compileSdk {
        version = release(COMPILE_SDK)
    }
    defaultConfig.minSdk = MIN_SDK
    compileOptions.sourceCompatibility = JavaVersion.VERSION_11
    compileOptions.targetCompatibility = JavaVersion.VERSION_11
}
