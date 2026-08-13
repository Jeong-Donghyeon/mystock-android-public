plugins {
    alias(libs.plugins.mystock.android.feature)
    alias(libs.plugins.mystock.screenshot)
}

android {
    namespace = "com.donghyeon.mystock.feature.account"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.activity.compose)
}
