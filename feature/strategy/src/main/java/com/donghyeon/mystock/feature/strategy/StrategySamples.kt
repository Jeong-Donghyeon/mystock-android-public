package com.donghyeon.mystock.feature.strategy

import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.core.model.StrategyRun
import com.donghyeon.mystock.core.model.StrategyVersion
import com.donghyeon.mystock.feature.common.sampleStrategies
import java.time.LocalDate

internal val sampleDetail = StrategyDetail(
    strategy = sampleStrategies.last(),
    note = "5일 이동평균이 20일 이동평균을 위로 지나면 추세 시작으로 보고 매수하고, " +
        "아래로 지나면 추세가 끝난 것으로 보고 전량 매도합니다. 신호는 장 마감 후 하루 한 번 계산하며, " +
        "대상은 국내·미국 대형주입니다.",
    versions = listOf(
        StrategyVersion("0.2.0", LocalDate.of(2026, 7, 14), "신호 계산을 장 마감 후 하루 한 번으로 변경"),
        StrategyVersion("0.1.9", LocalDate.of(2026, 6, 28), "손절 기준을 -2%로 조정"),
        StrategyVersion("0.1.5", LocalDate.of(2026, 6, 10), "대상 종목에 거래대금 하한 추가"),
        StrategyVersion("0.1.2", LocalDate.of(2026, 5, 30), "장 시작 직후 30분 매수 보류"),
        StrategyVersion("0.1.0", LocalDate.of(2026, 5, 28), "최초 배포"),
    ),
    runs = listOf(
        StrategyRun("run-3", "이동평균 교차", "0.2.0", "acc-1", "토스증권", RunOutcome.RUNNING, 17, Pnl(983_259, 17.93)),
        StrategyRun("run-2", "이동평균 교차", "0.1.9", "acc-1", "토스증권", RunOutcome.LIQUIDATED, 360, Pnl(412_000, 7.80)),
        StrategyRun("run-1", "이동평균 교차", "0.1.0", "acc-2", "한국투자증권", RunOutcome.CLOSED, 432, Pnl(180_000, 3.20)),
    ),
    startAccount = null,
)
