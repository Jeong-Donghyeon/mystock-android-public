import com.donghyeon.mystock.android
import com.donghyeon.mystock.configureAndroid
import com.donghyeon.mystock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("mystock.quality")
            android.configureAndroid()
            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
