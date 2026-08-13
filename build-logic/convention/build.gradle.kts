import io.gitlab.arturbosch.detekt.Detekt

plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.roborazzi.gradlePlugin)
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("../config/detekt/detekt.yml"))
    source.from(files("build.gradle.kts", "../settings.gradle.kts"))
}

tasks.withType<Detekt>().configureEach {
    autoCorrect = providers.gradleProperty("detektFix").isPresent
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "mystock.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "mystock.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "mystock.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "mystock.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "mystock.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("hilt") {
            id = "mystock.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("screenshot") {
            id = "mystock.screenshot"
            implementationClass = "ScreenshotConventionPlugin"
        }
        register("quality") {
            id = "mystock.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
