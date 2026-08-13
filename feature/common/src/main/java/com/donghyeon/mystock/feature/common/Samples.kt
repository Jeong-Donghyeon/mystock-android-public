package com.donghyeon.mystock.feature.common

import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyCondition

// 홈과 전략 목록·상세가 같은 전략 샘플로 프리뷰·스크린샷을 만든다
val sampleStrategies = listOf(
    Strategy(
        key = "empty",
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
