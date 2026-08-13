package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyCondition
import com.donghyeon.mystock.core.model.StrategyVersion
import java.time.LocalDate

// 주문을 내지 않는 전략이라 가짜 시장도 이 키에서는 매매하지 않는다
internal const val EMPTY_STRATEGY_KEY = "empty"

private const val USD_KRW = 1_380.0

internal fun Market.fxRate(): Double = when (this) {
    Market.KR -> 1.0
    Market.US -> USD_KRW
}

internal fun Market.currency(): Currency = when (this) {
    Market.KR -> Currency.KRW
    Market.US -> Currency.USD
}

internal data class FakeStock(val name: String, val market: Market, val price: Double)

internal val fakeStocks = listOf(
    FakeStock("삼성전자", Market.KR, 71_200.0),
    FakeStock("NAVER", Market.KR, 196_000.0),
    FakeStock("카카오", Market.KR, 44_350.0),
    FakeStock("현대차", Market.KR, 248_500.0),
    FakeStock("SK하이닉스", Market.KR, 187_300.0),
    FakeStock("기아", Market.KR, 108_900.0),
    FakeStock("AAPL", Market.US, 231.40),
    FakeStock("MSFT", Market.US, 394.20),
    FakeStock("NVDA", Market.US, 128.60),
    FakeStock("TSLA", Market.US, 246.80),
    FakeStock("GOOGL", Market.US, 176.30),
)

internal val fakeStrategies = listOf(
    Strategy(
        key = EMPTY_STRATEGY_KEY,
        displayName = "빈 전략 (검증용)",
        description = "매매하지 않는 검증용 전략",
        version = "1.0.0",
        markets = listOf(Market.KR),
        conditions = listOf(
            StrategyCondition("매수", "주문을 내지 않음"),
            StrategyCondition("매도", "주문을 내지 않음"),
            StrategyCondition("검증", "서버 연결·주기 동작만 확인"),
        ),
    ),
    Strategy(
        key = "breakout",
        displayName = "변동성 돌파",
        description = "전일 변동폭 기준 돌파 매수, 당일 청산",
        version = "0.3.1",
        markets = listOf(Market.KR, Market.US),
        conditions = listOf(
            StrategyCondition("매수", "전일 변동폭 × 0.5 상향 돌파 시 시장가 매수"),
            StrategyCondition("매도", "장 마감 10분 전 전량 매도"),
            StrategyCondition("손절", "매수가 대비 -2% 도달 시"),
        ),
    ),
    Strategy(
        key = "ma-cross",
        displayName = "이동평균 교차",
        description = "단기·장기 이동평균 교차로 매수·매도",
        version = "0.2.0",
        markets = listOf(Market.KR, Market.US),
        conditions = listOf(
            StrategyCondition("매수", "5일선이 20일선을 상향 돌파"),
            StrategyCondition("매도", "5일선이 20일선을 하향 돌파"),
            StrategyCondition("대상", "국내·미국 대형주"),
        ),
    ),
)

internal val fakeNotes = mapOf(
    EMPTY_STRATEGY_KEY to "주문을 전혀 내지 않고 서버 연결과 주기 동작만 확인합니다. 실제 계좌에 붙여도 잔고가 바뀌지 않습니다.",
    "breakout" to "전일 고가와 저가의 차이에 0.5를 곱한 값을 시가에 더한 가격을 넘어서면 시장가로 매수하고, " +
        "장 마감 10분 전에 전량 매도합니다. 매수가 대비 -2%에 닿으면 즉시 손절합니다.",
    "ma-cross" to "5일 이동평균이 20일 이동평균을 위로 지나면 추세 시작으로 보고 매수하고, " +
        "아래로 지나면 추세가 끝난 것으로 보고 전량 매도합니다. 신호는 장 마감 후 하루 한 번 계산하며, " +
        "대상은 국내·미국 대형주입니다.",
)

internal val fakeVersions = mapOf(
    EMPTY_STRATEGY_KEY to listOf(StrategyVersion("1.0.0", LocalDate.of(2026, 5, 1), "최초 배포")),
    "breakout" to listOf(
        StrategyVersion("0.3.1", LocalDate.of(2026, 7, 2), "손절 기준을 -2%로 조정"),
        StrategyVersion("0.3.0", LocalDate.of(2026, 6, 20), "미국 시장 추가"),
        StrategyVersion("0.2.0", LocalDate.of(2026, 6, 1), "최초 배포"),
    ),
    "ma-cross" to listOf(
        StrategyVersion("0.2.0", LocalDate.of(2026, 7, 14), "신호 계산을 장 마감 후 하루 한 번으로 변경"),
        StrategyVersion("0.1.9", LocalDate.of(2026, 6, 28), "손절 기준을 -2%로 조정"),
        StrategyVersion("0.1.5", LocalDate.of(2026, 6, 10), "대상 종목에 거래대금 하한 추가"),
        StrategyVersion("0.1.2", LocalDate.of(2026, 5, 30), "장 시작 직후 30분 매수 보류"),
        StrategyVersion("0.1.0", LocalDate.of(2026, 5, 28), "최초 배포"),
    ),
)
