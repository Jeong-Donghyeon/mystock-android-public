import com.android.build.api.dsl.ApplicationExtension
import com.donghyeon.mystock.TARGET_SDK
import com.donghyeon.mystock.android
import com.donghyeon.mystock.configureAndroid
import com.donghyeon.mystock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("mystock.quality")
            android.configureAndroid()
            extensions.configure<ApplicationExtension> {
                defaultConfig.targetSdk = TARGET_SDK
            }
            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
