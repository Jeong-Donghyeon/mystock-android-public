import java.util.Properties

plugins {
    alias(libs.plugins.mystock.android.library)
    alias(libs.plugins.mystock.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// 서버 주소는 gitignore된 keystore.properties에 둔다. 파일이 없는 클론에서도 빌드는 돌아야 해 기본값을 둔다
val baseUrl: String? = Properties().apply {
    providers.fileContents(isolated.rootProject.projectDirectory.file("keystore.properties"))
        .asText.orNull?.let { load(it.reader()) }
}.getProperty("mystock.baseUrl", "http://localhost:8080/api/v1/")

android {
    namespace = "com.donghyeon.mystock.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    api(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
}
