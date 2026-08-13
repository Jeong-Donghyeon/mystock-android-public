import com.donghyeon.mystock.android
import com.donghyeon.mystock.libs
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

private const val SCREENSHOT_DIR = "screenshot"

class ScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")
            android.testOptions.unitTests.isIncludeAndroidResources = true

            val moduleName = path.removePrefix(":").replace(':', '-')
            val goldenDir = rootProject.layout.projectDirectory.dir(SCREENSHOT_DIR).dir(moduleName)

            extensions.configure<RoborazziExtension> {
                outputDir.set(goldenDir)
                compare.outputDir.set(layout.buildDirectory.dir("outputs/roborazzi/compare"))
            }

            val goldens = goldenDir.asFile
            val goldensRecorded = tasks.register("checkScreenshotGoldens") {
                group = "verification"
                description = "스크린샷 골든이 기록되어 있는지 검사"
                doLast {
                    if (goldens.walk().none { it.extension == "png" }) {
                        throw GradleException(
                            "스크린샷 골든이 없어 verify가 검증이 아니다: ${goldens.path}. " +
                                "확인을 받아 recordRoborazziDebug를 돌린다",
                        )
                    }
                }
            }
            tasks.configureEach {
                if (name == "verifyRoborazziDebug") dependsOn(goldensRecorded)
            }
            tasks.withType<Test>().configureEach { mustRunAfter(goldensRecorded) }

            dependencies {
                "testImplementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
                "testImplementation"(libs.findLibrary("robolectric").get())
                "testImplementation"(libs.findLibrary("roborazzi").get())
                "testImplementation"(libs.findLibrary("roborazzi-compose").get())
                "testImplementation"(libs.findLibrary("roborazzi-junit-rule").get())
                "testImplementation"(
                    libs.findLibrary("androidx-compose-ui-test-junit4").get(),
                )
                "debugImplementation"(
                    libs.findLibrary("androidx-compose-ui-test-manifest").get(),
                )
            }
        }
    }
}
