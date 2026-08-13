plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.mystock.quality)
}

tasks.named("detekt") {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:detekt"))
}

tasks.register("verifyAll") {
    group = "verification"
    description = "마무리 검증. 모든 모듈의 빌드·테스트·스크린샷·린트·정적 검사를 돌리고 통과 표식을 남긴다"
    val verifying = setOf(
        "assembleDebug",
        "test",
        "verifyRoborazziDebug",
        "lint",
        "detekt",
        "checkDesignTokens",
    )
    allprojects.forEach { module -> dependsOn(module.tasks.named { it in verifying }) }
    dependsOn(gradle.includedBuild("build-logic").task(":convention:detekt"))
    val marker = layout.buildDirectory.file("verified")
    doLast {
        marker.get().asFile.apply {
            parentFile.mkdirs()
            writeText("")
        }
    }
}
