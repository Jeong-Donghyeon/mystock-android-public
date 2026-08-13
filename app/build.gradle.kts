plugins {
    alias(libs.plugins.mystock.android.application)
    alias(libs.plugins.mystock.android.compose)
    alias(libs.plugins.mystock.hilt)
    alias(libs.plugins.oss.licenses)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.donghyeon.mystock"
    defaultConfig {
        applicationId = "com.donghyeon.mystock"
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures {
        buildConfig = true
    }
    // onNewToken은 SDK가 onRegistered로 대체하며 deprecate했는데 검사는 아직 그걸 요구한다
    lint.disable += "MissingFirebaseInstanceTokenRefresh"
    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }
}

dependencies {
    implementation(project(":design"))
    implementation(project(":core:data"))
    implementation(project(":feature:account"))
    implementation(project(":feature:common"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:strategy"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.play.services.oss.licenses)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    testImplementation(libs.robolectric)
}
