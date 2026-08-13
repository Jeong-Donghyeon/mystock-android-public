import com.donghyeon.mystock.SourceRuleCheckTask
import com.donghyeon.mystock.android
import com.donghyeon.mystock.libs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

private val Project.isScreenModule: Boolean
    get() = path == ":app" || path.startsWith(":feature")

private val VERSION_UPDATE_CHECKS =
    setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")

private val DESIGN_TOKEN_RULES = mapOf(
    """\bColor\s*\(""" to "Color(...) 대신 MyStockTheme.colors 토큰",
    """\b\d+(\.\d+)?\.sp\b""" to "직접 sp 값 대신 MyStockTheme.typography 토큰",
    """\b(?!0\.dp\b)\d+(\.\d+)?\.dp\b""" to "0이 아닌 dp 값 대신 MyStockTheme 토큰이나 컴포넌트",
)

class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            pluginManager.withPlugin("com.android.base") {
                with(android.lint) {
                    warningsAsErrors = true
                    disable.addAll(VERSION_UPDATE_CHECKS)
                }
            }

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(rootProject.file("config/detekt/detekt.yml"))
                source.from(files("build.gradle.kts", "settings.gradle.kts").filter { it.exists() })
            }

            dependencies {
                "detektPlugins"(
                    "io.gitlab.arturbosch.detekt:detekt-formatting:" +
                        libs.findVersion("detekt").get().requiredVersion,
                )
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
                dependencies {
                    "detektPlugins"(libs.findLibrary("compose-rules-detekt").get())
                }
                extensions.configure<DetektExtension> {
                    config.from(rootProject.file("config/detekt/compose.yml"))
                }
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = JavaVersion.VERSION_11.toString()
                autoCorrect = providers.gradleProperty("detektFix").isPresent
            }

            if (isScreenModule) {
                val title = "디자인 토큰을 쓰지 않은 곳"
                tasks.register<SourceRuleCheckTask>("checkDesignTokens") {
                    group = "verification"
                    description = "$title 검사"
                    this.title.set(title)
                    rules.set(DESIGN_TOKEN_RULES)
                    sources.from(
                        layout.projectDirectory.dir("src").asFileTree.matching { include("**/*.kt") },
                    )
                    baseDirectory.set(rootProject.layout.projectDirectory)
                    report.set(layout.buildDirectory.file("reports/checkDesignTokens.txt"))
                }
            }
        }
    }
}
