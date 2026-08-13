plugins {
    alias(libs.plugins.mystock.android.feature)
    alias(libs.plugins.mystock.screenshot)
}

android {
    namespace = "com.donghyeon.mystock.feature.strategy"
}

dependencies {
    implementation(project(":core:data"))
}
