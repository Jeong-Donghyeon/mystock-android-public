import com.donghyeon.mystock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("java-library")
            pluginManager.apply("mystock.quality")
            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain { languageVersion.set(JavaLanguageVersion.of(11)) }
            }
            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
