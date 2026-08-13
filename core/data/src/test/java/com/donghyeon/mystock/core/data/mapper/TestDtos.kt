package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.network.dto.PnlDto
import com.donghyeon.mystock.core.network.dto.RunSummaryDto
import com.donghyeon.mystock.core.network.dto.StrategyConditionDto
import com.donghyeon.mystock.core.network.dto.StrategyDto

fun testStrategyDto(markets: List<String> = listOf("KR", "US")) = StrategyDto(
    key = "ma-cross",
    displayName = "이동평균 교차",
    description = "단기·장기 이동평균 교차로 매수·매도",
    version = "0.2.0",
    markets = markets,
    conditions = listOf(StrategyConditionDto("매수", "5일선이 20일선을 상향 돌파")),
)

fun testRunDto(outcome: String = "RUNNING") = RunSummaryDto(
    id = "run-5",
    strategyName = "이동평균 교차",
    version = "0.2.0",
    accountId = "toss-live",
    accountName = "토스증권",
    outcome = outcome,
    hours = 17,
    pnl = PnlDto(983_259, 17.93),
)
