package com.donghyeon.mystock.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

private const val COMPONENT = "com.donghyeon.mystock.design.component"

class DesignConventionTest {

    private val scope = Konsist.scopeFromProduction()

    private val document = File("../docs/DESIGN.md").readText()

    private val mappingSection = document
        .substringAfter("## 원천 ↔ 코드 대응")
        .substringBefore("\n## ")

    // 대응표는 `이름`이나 `이름(인자)` 꼴로 코드를 가리킨다
    private fun mentions(name: String) = Regex("`$name[`(]").containsMatchIn(mappingSection)

    @Test
    fun `design의 공개 컴포넌트는 전부 대응표에 있다`() {
        scope.functions()
            .withPackage(COMPONENT)
            .filter { it.hasPublicOrDefaultModifier && it.hasAnnotationWithName("Composable") && !it.hasReturnType() }
            .assertTrue { mentions(it.name) }
    }

    @Test
    fun `대응표가 가리키는 이름은 전부 코드에 있는 선언이다`() {
        val declared = (scope.classesAndInterfacesAndObjects() + scope.functions()).map { it.name }.toSet()
        val documented = Regex("`([A-Z][A-Za-z]*)[`(]").findAll(mappingSection).map { it.groupValues[1] }.toSet()

        assertEquals(emptySet<String>(), documented - declared)
    }
}
