package com.donghyeon.mystock.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

private const val MODEL = "com.donghyeon.mystock.core.model"
private const val NETWORK = "com.donghyeon.mystock.core.network"
private const val DTO = "$NETWORK.dto"
private const val DATA = "com.donghyeon.mystock.core.data"
private const val FAKE = "$DATA.fake"
private const val MAPPER = "$DATA.mapper"
private const val REMOTE = "$DATA.remote"

private val NETWORK_LIBRARIES = listOf("retrofit2.", "okhttp3.", "kotlinx.serialization.")

class CoreConventionTest {

    private val scope = Konsist.scopeFromProduction()

    @Test
    fun `모델은 직렬화·네트워크·Android를 모른다`() {
        scope.files
            .withPackage(MODEL)
            .assertTrue { file -> file.imports.all { it.name.startsWith("java.") || it.name.startsWith("kotlin.") } }
    }

    @Test
    fun `DTO는 dto 패키지의 Serializable data class고 이름이 Dto로 끝난다`() {
        scope.classes()
            .withPackage(DTO)
            .assertTrue { it.name.endsWith("Dto") && it.hasDataModifier && it.hasAnnotationWithName("Serializable") }
    }

    @Test
    fun `core에서 Serializable은 dto 패키지에만 붙는다`() {
        scope.classesAndObjects()
            .withPackage("com.donghyeon.mystock.core..")
            .filter { it.hasAnnotationWithName("Serializable") }
            .assertTrue { it.resideInPackage(DTO) }
    }

    @Test
    fun `DTO는 기본값이 없어 계약에 있는 필드가 빠지면 디코딩이 실패한다`() {
        scope.classes()
            .withPackage(DTO)
            .flatMap { it.primaryConstructor?.parameters.orEmpty() }
            .assertFalse { it.hasDefaultValue() }
    }

    @Test
    fun `API 인터페이스는 network 패키지에 있고 전부 suspend이며 DTO를 돌려준다`() {
        scope.interfaces()
            .withNameEndingWith("Api")
            .assertTrue { api ->
                api.resideInPackage(NETWORK) &&
                    api.functions().all { function ->
                        function.hasSuspendModifier &&
                            (function.returnType == null || function.returnType?.name.orEmpty().contains("Dto"))
                    }
            }
    }

    @Test
    fun `core의 Hilt 모듈은 di 패키지의 internal 선언이다`() {
        scope.classesAndObjects()
            .withPackage("com.donghyeon.mystock.core..")
            .filter { it.hasAnnotationWithName("Module") }
            .assertTrue { it.hasInternalModifier && it.packagee?.name?.endsWith(".di") == true }
    }

    @Test
    fun `리포지토리는 예외 대신 Result를 돌려주고 관찰은 Flow of Result나 StateFlow다`() {
        scope.interfaces()
            .withPackage(DATA)
            .withNameEndingWith("Repository")
            .assertTrue { repository ->
                repository.functions().all { function ->
                    val returnType = function.returnType?.name.orEmpty()
                    if (function.hasSuspendModifier) {
                        returnType.startsWith("Result<")
                    } else {
                        returnType.startsWith("Flow<Result<")
                    }
                } &&
                    repository.properties().all { it.type?.name.orEmpty().startsWith("StateFlow<") }
            }
    }

    @Test
    fun `리포지토리 구현은 internal Singleton이고 Fake는 fake Remote는 remote 패키지다`() {
        scope.classes()
            .withNameEndingWith("Repository")
            .assertTrue { clazz ->
                val contract = clazz.name.removePrefix("Fake").removePrefix("Remote")
                clazz.resideInPackage(if (clazz.name.startsWith("Fake")) FAKE else REMOTE) &&
                    clazz.hasInternalModifier &&
                    clazz.hasAnnotationWithName("Singleton") &&
                    contract != clazz.name &&
                    clazz.parents().any { it.name == contract }
            }
    }

    @Test
    fun `Fake로 시작하는 파일은 fake 패키지에 있다`() {
        scope.files
            .withPackage("$DATA..")
            .filter { it.name.startsWith("Fake") }
            .assertTrue { it.packagee?.name == FAKE }
    }

    @Test
    fun `DTO 매핑은 mapper 패키지의 Mappers 파일에 있는 internal toDomain뿐이다`() {
        scope.functions()
            .filter { it.receiverType?.name?.endsWith("Dto") == true }
            .assertTrue {
                it.name == "toDomain" &&
                    it.hasInternalModifier &&
                    it.resideInPackage(MAPPER) &&
                    it.containingFile.name.endsWith("Mappers")
            }
    }

    @Test
    fun `data는 Retrofit·OkHttp·직렬화를 직접 다루지 않는다`() {
        scope.files
            .withPackage("$DATA..")
            .assertFalse { file -> file.imports.any { import -> NETWORK_LIBRARIES.any { import.name.startsWith(it) } } }
    }
}
