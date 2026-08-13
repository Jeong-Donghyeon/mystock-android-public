import com.donghyeon.mystock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("mystock.android.library")
            pluginManager.apply("mystock.android.compose")
            pluginManager.apply("mystock.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            dependencies {
                "api"(project(":core:model"))
                "api"(libs.findLibrary("androidx-navigation3-runtime").get())
                "implementation"(project(":design"))
                "implementation"(project(":feature:common"))
                "implementation"(libs.findLibrary("kotlinx-serialization-json").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())
                "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
