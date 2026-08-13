package com.donghyeon.mystock.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

private const val FEATURE_PACKAGE = "com.donghyeon.mystock.feature."
private const val COMMON = "common"

class ArchitectureTest {

    private val model = Layer("core:model", "com.donghyeon.mystock.core.model..")
    private val network = Layer("core:network", "com.donghyeon.mystock.core.network..")
    private val data = Layer("core:data", "com.donghyeon.mystock.core.data..")
    private val design = Layer("design", "com.donghyeon.mystock.design..")
    private val feature = Layer("feature", "com.donghyeon.mystock.feature..")

    @Test
    fun `모듈 의존은 한 방향으로만 흐른다`() {
        Konsist.scopeFromProduction().assertArchitecture {
            // dependsOn은 strict가 아니면 다른 의존을 막지 않으므로 금지 방향을 doesNotDependOn으로 적는다
            model.dependsOnNothing()
            design.dependsOnNothing()
            network.doesNotDependOn(data, design, feature)
            data.doesNotDependOn(design, feature)
        }
    }

    @Test
    fun `feature 모듈은 자기 모듈과 common만 참조하고 network를 직접 쓰지 않는다`() {
        Konsist.scopeFromProduction().files
            .withPackage("$FEATURE_PACKAGE.")
            .assertTrue { file ->
                val module = file.packagee?.name?.featureModule()
                file.imports.none { it.name.startsWith("com.donghyeon.mystock.core.network") } &&
                    (module != COMMON || file.imports.none { it.name.startsWith("com.donghyeon.mystock.core.data") }) &&
                    file.imports
                        .filter { it.name.startsWith(FEATURE_PACKAGE) }
                        .all { import ->
                            val target = import.name.featureModule()
                            target == module || (target == COMMON && module != COMMON)
                        }
            }
    }

    @Test
    fun `feature는 NavKey와 entry 등록만 알고 백스택은 app이 든다`() {
        Konsist.scopeFromProduction().files
            .withPackage("$FEATURE_PACKAGE.")
            .assertTrue { file ->
                file.imports
                    .filter { it.name.startsWith("androidx.navigation3.") }
                    .all { it.name in NAVIGATION_IMPORTS_ALLOWED_IN_FEATURE }
            }
    }

    private fun String.featureModule(): String = removePrefix(FEATURE_PACKAGE).substringBefore('.')
}

private val NAVIGATION_IMPORTS_ALLOWED_IN_FEATURE = setOf(
    "androidx.navigation3.runtime.NavKey",
    "androidx.navigation3.runtime.EntryProviderScope",
)
