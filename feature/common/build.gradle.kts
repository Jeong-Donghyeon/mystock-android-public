plugins {
    alias(libs.plugins.mystock.android.library)
}

android {
    namespace = "com.donghyeon.mystock.feature.common"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":design"))
}
