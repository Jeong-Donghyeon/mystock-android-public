package com.donghyeon.mystock.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ScreenConventionTest {

    private val scope = Konsist.scopeFromProduction()

    private val viewModels = scope.classes()
        .filter { clazz -> clazz.parents().any { it.name == "ViewModel" } }

    private val uiStates = scope.classes().withNameEndingWith("UiState")

    // 샘플·픽스처와 포맷 단위는 사용자에게 보이는 문구가 아니라 데이터다
    private val copyFiles = scope.files
        .withPackage("com.donghyeon.mystock.feature..")
        .filterNot { it.name.endsWith("Samples") || it.name.endsWith("Contract") || it.name == "Formats" }

    private fun screenFunction(screen: String, packageName: String?, suffix: String): KoFunctionDeclaration? =
        scope.functions().firstOrNull {
            it.name == screen + suffix && it.packagee?.name == packageName && it.hasAnnotationWithName("Composable")
        }

    @Test
    fun `ViewModel은 feature 패키지에 두고 이름을 ViewModel로 끝낸다`() {
        viewModels.assertTrue {
            it.name.endsWith("ViewModel") && it.resideInPackage("com.donghyeon.mystock.feature..")
        }
    }

    @Test
    fun `ViewModel은 HiltViewModel로 주입한다`() {
        viewModels.assertTrue { clazz -> clazz.annotations.any { it.name == "HiltViewModel" } }
    }

    @Test
    fun `ViewModel의 공개 API는 uiState·effects·onIntent뿐이다`() {
        viewModels.assertTrue { clazz ->
            val screen = clazz.name.removeSuffix("ViewModel")
            // 중첩 Factory의 create는 ViewModel의 API가 아니다
            val functions = clazz.functions(includeNested = false).filter { it.hasPublicOrDefaultModifier }
            val properties = clazz.properties(includeNested = false).filter { it.hasPublicOrDefaultModifier }
            functions.map { it.name to it.parameters.map { param -> param.type.name } } ==
                listOf("onIntent" to listOf("${screen}UiIntent")) &&
                properties.map { it.name to it.type?.name } == listOf(
                    "uiState" to "StateFlow<${screen}UiState>",
                    "effects" to "Flow<${screen}UiEffect>",
                )
        }
    }

    @Test
    fun `화면마다 ViewModel·Screen·Route가 Contract와 같은 패키지에 있다`() {
        uiStates.assertTrue { state ->
            val screen = state.name.removeSuffix("UiState")
            val packageName = state.packagee?.name
            scope.classes().any { it.name == "${screen}ViewModel" && it.packagee?.name == packageName } &&
                screenFunction(screen, packageName, "Screen") != null &&
                screenFunction(screen, packageName, "Route") != null
        }
    }

    @Test
    fun `Screen은 state·onIntent·modifier만 받는다`() {
        uiStates.assertTrue { state ->
            val screen = state.name.removeSuffix("UiState")
            screenFunction(screen, state.packagee?.name, "Screen")
                ?.parameters
                ?.map { it.name to it.type.name } == listOf(
                "state" to "${screen}UiState",
                "onIntent" to "(${screen}UiIntent) -> Unit",
                "modifier" to "Modifier",
            )
        }
    }

    @Test
    fun `Route는 modifier와 on 콜백을 받고 viewModel은 hiltViewModel 기본값이다`() {
        uiStates.assertTrue { state ->
            val screen = state.name.removeSuffix("UiState")
            val parameters = screenFunction(screen, state.packagee?.name, "Route")?.parameters.orEmpty()
            // 인자가 있는 화면은 첫 파라미터로 <Screen>Key를 받고 ViewModel 팩토리에 넘긴다
            val hasKey = parameters.firstOrNull()?.let { it.name == "key" && it.type.name == "${screen}Key" } == true
            val rest = parameters.drop(if (hasKey) 1 else 0)
            val callbacks = rest.drop(1).dropLast(1)
            val expectedDefault = if (hasKey) {
                "hiltViewModel<${screen}ViewModel, ${screen}ViewModel.Factory>(creationCallback = { it.create(key) })"
            } else {
                "hiltViewModel()"
            }
            rest.firstOrNull()?.name == "modifier" &&
                callbacks.all { it.name.startsWith("on") && it.type.name.endsWith("-> Unit") } &&
                rest.lastOrNull()?.let {
                    it.name == "viewModel" &&
                        it.type.name == "${screen}ViewModel" &&
                        it.defaultValue?.replace(Regex("\\s"), "")?.replace(",)", ")") ==
                        expectedDefault.replace(" ", "")
                } == true
        }
    }

    @Test
    fun `상태는 dispatch가 부르는 파일 최상위 reduce에서만 바꾼다`() {
        viewModels.assertTrue { clazz ->
            val screen = clazz.name.removeSuffix("ViewModel")
            val reduce = clazz.containingFile.functions().firstOrNull { it.name == "reduce" }
            val mutators = clazz.functions()
                .filter { it.text.contains("_uiState.update") || it.text.contains("_uiState.value =") }
            reduce != null &&
                reduce.hasPrivateModifier &&
                reduce.returnType?.name == "${screen}UiState" &&
                reduce.parameters.map { it.type.name } == listOf("${screen}UiState", "${screen}Msg") &&
                mutators.map { it.name } == listOf("dispatch")
        }
    }

    @Test
    fun `Msg는 ViewModel 파일의 private sealed interface다`() {
        scope.interfaces()
            .withNameEndingWith("Msg")
            .assertTrue {
                it.hasPrivateModifier && it.hasSealedModifier && it.containingFile.name.endsWith("ViewModel")
            }
    }

    @Test
    fun `UiState는 Contract 파일의 data class다`() {
        uiStates.assertTrue { it.hasDataModifier && it.containingFile.name.endsWith("Contract") }
    }

    @Test
    fun `UiIntent·UiEffect·NavTarget·Content는 Contract 파일의 sealed interface다`() {
        scope.interfaces()
            .withPackage("com.donghyeon.mystock.feature..")
            .withNameEndingWith("UiIntent", "UiEffect", "NavTarget", "Content")
            .assertTrue { it.hasSealedModifier && it.containingFile.name.endsWith("Contract") }
    }

    @Test
    fun `스낵바 호스트는 app이 하나만 두고 feature는 LocalSnackbarHostState로 받는다`() {
        scope.files
            .withPackage("com.donghyeon.mystock.feature..")
            .assertFalse {
                it.hasImportWithName(
                    "androidx.compose.material3.SnackbarHostState",
                    "com.donghyeon.mystock.design.component.MsSnackbarHost",
                )
            }
    }

    @Test
    fun `feature는 showSnackbar를 직접 부르지 않고 SnackbarController로 보여 준다`() {
        scope.files
            .withPackage("com.donghyeon.mystock.feature..")
            .assertFalse { it.text.contains("showSnackbar(") }
    }

    @Test
    fun `화면 문구는 strings xml에 두고 코드에 한글 리터럴을 남기지 않는다`() {
        copyFiles.assertFalse { KOREAN_LITERAL.containsMatchIn(codeOf(it.text)) }
    }

    @Test
    fun `ViewModel 파일의 최상위 함수는 reduce뿐이다`() {
        viewModels.assertTrue { clazz ->
            clazz.containingFile.functions(includeNested = false, includeLocal = false)
                .all { it.name.startsWith("reduce") }
        }
    }
}

private val KOREAN_LITERAL = Regex("\"[^\"\\n]*[가-힣]")

private val BLOCK_COMMENT = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL)

// 주석에 적은 한글은 문구가 아니다. 문자열 안의 //는 주석이 아니므로 따옴표 개수로 가른다
private fun codeOf(text: String): String = text
    .replace(BLOCK_COMMENT, "")
    .lines()
    .joinToString("\n") { it.take(commentStart(it)) }

private fun commentStart(line: String): Int {
    var quotes = 0
    for (i in 0..<line.length - 1) {
        if (line[i] == '"') quotes++
        if (line[i] == '/' && line[i + 1] == '/' && quotes % 2 == 0) return i
    }
    return line.length
}
