package com.donghyeon.mystock.core.network.dto

import com.donghyeon.mystock.core.network.apiJson
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 픽스처는 여기서 고치지 않는다. 규칙은 `docs/API.md`에 있다. */
class ContractTest {

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/contract/$name")) { "픽스처가 없다: $name" }.readText()

    private inline fun <reified T> decode(name: String): T = apiJson.decodeFromString<T>(fixture(name))

    private inline fun <reified T> decode(element: JsonElement): T = apiJson.decodeFromJsonElement<T>(element)

    @Test
    fun `계좌 응답은 운용 중·일시정지·대기 모두 AccountDto로 디코딩된다`() {
        val running = decode<AccountDto>("account.json")
        val paused = decode<AccountDto>("account-paused.json")
        val waiting = decode<AccountDto>("account-waiting.json")

        assertEquals("RUNNING", running.state)
        assertEquals("ma-cross", running.strategy?.key)
        assertNull(running.pauseReason)
        assertEquals("PAUSED", paused.state)
        assertEquals("브로커 오류가 3회 연속으로 일어나 매매를 멈췄습니다", paused.pauseReason)
        assertEquals("WAITING", waiting.state)
        assertNull(waiting.strategy)
        assertNull(waiting.cumulativePnl)
    }

    @Test
    fun `전략 응답은 StrategyDto로 디코딩된다`() {
        val strategy = decode<StrategyDto>("strategy.json")

        assertEquals(listOf("KR", "US"), strategy.markets)
        assertEquals(2, strategy.conditions.size)
    }

    @Test
    fun `전략 상세 응답은 시작 계좌가 있든 없든 StrategyDetailDto로 디코딩된다`() {
        val withAccount = decode<StrategyDetailDto>("strategy-detail.json")
        val withoutAccount = decode<StrategyDetailDto>("strategy-detail-no-account.json")

        assertEquals("2026-07-14", withAccount.versions.single().releasedAt)
        assertEquals("RUNNING", withAccount.runs.single().outcome)
        assertEquals(5_483_000L, withAccount.startAccount?.cashAmount)
        assertNull(withoutAccount.startAccount)
    }

    @Test
    fun `운용 내역 응답은 RunsDto로 디코딩된다`() {
        val runs = decode<RunsDto>("runs.json")

        assertEquals("토스증권", runs.accountName)
        assertEquals(17, runs.runs.single().hours)
    }

    @Test
    fun `포트폴리오 응답은 PortfolioDto로 디코딩된다`() {
        val portfolio = decode<PortfolioDto>("portfolio.json")

        assertEquals(231.40, portfolio.holdings.single().currentPrice.value, 0.0)
        assertEquals("USD", portfolio.holdings.single().currentPrice.currency)
    }

    @Test
    fun `성과 응답은 PerformanceDto로 디코딩된다`() {
        val performance = decode<PerformanceDto>("performance.json")

        assertEquals(5_483_000L, performance.initialEquity)
        assertEquals("2026-09-12T00:05:00Z", performance.startedAt)
        assertNotNull(performance.todayPnl)
    }

    @Test
    fun `주문 페이지 응답은 OrderPageDto로 디코딩된다`() {
        val page = decode<OrderPageDto>("order-page.json")

        assertEquals(12L, page.totalCount)
        assertEquals(listOf("SELL", "BUY"), page.orders.map { it.side })
        assertNull(page.orders.last().realizedPnl)
    }

    @Test
    fun `소수점 수량 응답도 같은 DTO로 디코딩된다`() {
        val portfolio = decode<PortfolioDto>("portfolio-fractional.json")
        val page = decode<OrderPageDto>("order-page-fractional.json")

        assertEquals(2.5, portfolio.holdings.single().quantity, 0.0)
        assertEquals(2.5, page.orders.single().quantity, 0.0)
    }

    @Test
    fun `토큰 응답은 TokensDto로 디코딩된다`() {
        val tokens = decode<TokensDto>("tokens.json")

        assertEquals("2026-09-14T03:20:00Z", tokens.expiresAt)
        assertTrue(tokens.accessToken.isNotBlank() && tokens.refreshToken.isNotBlank())
    }

    @Test
    fun `오류 응답은 ErrorDto로 디코딩된다`() {
        val error = decode<ErrorDto>("error.json")

        assertEquals("INVALID_TRANSITION", error.code)
        assertEquals("대기 계좌는 일시정지할 수 없습니다", error.message)
    }

    @Test
    fun `nullable에 적힌 경로는 null이 와도 디코딩된다`() {
        nullablePaths().forEach { (name, paths) ->
            val nulled = paths.fold(apiJson.parseToJsonElement(fixture(name))) { json, path -> json.nulled(path) }
            decoders.getValue(name)(nulled)
        }
    }

    // 객체형 필드는 검사하지 않는다 — DTO가 상태별로 나뉘지 않아 다른 상태의 픽스처에서는 null일 수 있다
    @Test
    fun `nullable에 없는 경로는 어느 픽스처든 null이 오면 디코딩이 실패한다`() {
        val nullable = nullablePaths()
        decoders.forEach { (name, decode) ->
            val json = apiJson.parseToJsonElement(fixture(name))
            val strict = json.leafPaths()
                .filter { it !in nullable[name].orEmpty() && json.at(it).any { leaf -> leaf !is JsonNull } }
            strict.forEach { path ->
                assertThrows("$name $path", SerializationException::class.java) { decode(json.nulled(path)) }
            }
        }
    }

    @Test
    fun `어느 픽스처든 키가 빠지면 null이 올 수 있는 필드라도 디코딩이 실패한다`() {
        decoders.forEach { (name, decode) ->
            val json = apiJson.parseToJsonElement(fixture(name))
            json.keyPaths().forEach { path ->
                assertThrows("$name $path", SerializationException::class.java) { decode(json.without(path)) }
            }
        }
    }

    @Test
    fun `모든 픽스처가 decoders에 등록되어 있다`() {
        // Gradle 유닛 테스트는 모듈 루트에서 돈다
        val dir = File("src/test/resources/contract")
        val fixtures = checkNotNull(dir.list()) { "계약 픽스처 폴더가 없다: ${dir.absolutePath}" }
            .filter { it.endsWith(".json") && it != "nullable.json" }

        assertEquals(fixtures.sorted(), decoders.keys.sorted())
    }

    private val decoders: Map<String, (JsonElement) -> Any> =
        mapOf(
            "account.json" to { decode<AccountDto>(it) },
            "account-paused.json" to { decode<AccountDto>(it) },
            "account-waiting.json" to { decode<AccountDto>(it) },
            "strategy.json" to { decode<StrategyDto>(it) },
            "strategy-detail.json" to { decode<StrategyDetailDto>(it) },
            "strategy-detail-no-account.json" to { decode<StrategyDetailDto>(it) },
            "runs.json" to { decode<RunsDto>(it) },
            "portfolio.json" to { decode<PortfolioDto>(it) },
            "portfolio-fractional.json" to { decode<PortfolioDto>(it) },
            "performance.json" to { decode<PerformanceDto>(it) },
            "order-page.json" to { decode<OrderPageDto>(it) },
            "order-page-fractional.json" to { decode<OrderPageDto>(it) },
            "tokens.json" to { decode<TokensDto>(it) },
            "error.json" to { decode<ErrorDto>(it) },
        )

    private fun nullablePaths(): Map<String, List<String>> =
        apiJson.parseToJsonElement(fixture("nullable.json")).jsonObject
            .mapValues { (_, paths) -> paths.jsonArray.map { it.jsonPrimitive.content } }
}

private fun String.segments(): List<String> =
    removePrefix("$").split('.').filter { it.isNotEmpty() }

private fun JsonElement.nulled(path: String): JsonElement = nulled(path.segments())

// 배열 원소 중 이미 null인 것은 그 아래를 건드릴 수 없으니 그대로 둔다
private fun JsonElement.nulled(segments: List<String>): JsonElement = when {
    segments.isEmpty() -> JsonNull
    this !is JsonObject -> this
    else -> {
        val head = segments.first()
        val tail = segments.drop(1)
        val key = head.removeSuffix("[*]")
        val child = getValue(key)
        val replaced = if (head.endsWith("[*]")) {
            JsonArray(child.jsonArray.map { it.nulled(tail) })
        } else {
            child.nulled(tail)
        }
        JsonObject(this + (key to replaced))
    }
}

private fun JsonElement.without(path: String): JsonElement = without(path.segments())

private fun JsonElement.without(segments: List<String>): JsonElement = when {
    this !is JsonObject -> this
    segments.size == 1 -> JsonObject(this - segments.single().removeSuffix("[*]"))
    else -> {
        val head = segments.first()
        val tail = segments.drop(1)
        val key = head.removeSuffix("[*]")
        val child = getValue(key)
        val replaced = if (head.endsWith("[*]")) {
            JsonArray(child.jsonArray.map { it.without(tail) })
        } else {
            child.without(tail)
        }
        JsonObject(this + (key to replaced))
    }
}

private fun JsonElement.at(path: String): List<JsonElement> = at(path.segments())

private fun JsonElement.at(segments: List<String>): List<JsonElement> = when {
    segments.isEmpty() -> listOf(this)
    this !is JsonObject -> emptyList()
    else -> {
        val head = segments.first()
        val child = getValue(head.removeSuffix("[*]"))
        val tail = segments.drop(1)
        if (head.endsWith("[*]")) child.jsonArray.flatMap { it.at(tail) } else child.at(tail)
    }
}

// 객체의 모든 키 경로. 배열 원소는 첫 원소가 형태를 정하므로 [*]로 합친다
private fun JsonElement.keyPaths(prefix: String = "$"): List<String> = when (this) {
    is JsonObject -> entries.flatMap { (key, value) -> listOf("$prefix.$key") + value.keyPaths("$prefix.$key") }
    is JsonArray -> firstOrNull()?.keyPaths("$prefix[*]").orEmpty()
    else -> emptyList()
}

// 배열 원소는 첫 원소가 형태를 정하므로 경로는 [*]로 합친다
private fun JsonElement.leafPaths(prefix: String = "$"): List<String> = when (this) {
    is JsonObject -> entries.flatMap { (key, value) -> value.leafPaths("$prefix.$key") }
    is JsonArray -> firstOrNull()?.leafPaths("$prefix[*]").orEmpty()
    else -> listOf(prefix)
}
