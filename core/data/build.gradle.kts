plugins {
    alias(libs.plugins.mystock.android.library)
    alias(libs.plugins.mystock.hilt)
}

android {
    namespace = "com.donghyeon.mystock.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:network"))
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
}
