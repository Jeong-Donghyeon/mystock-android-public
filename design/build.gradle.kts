plugins {
    alias(libs.plugins.mystock.android.library)
    alias(libs.plugins.mystock.android.compose)
    alias(libs.plugins.mystock.screenshot)
}

version = "0.1.0"

android {
    namespace = "com.donghyeon.mystock.design"
}

dependencies {
    // 오버레이 테스트가 다이얼로그의 백 디스패처를 직접 누른다
    testImplementation(libs.androidx.activity.compose)
}
