package com.donghyeon.mystock.core.model

data class Strategy(
    val key: String,
    val displayName: String,
    val description: String,
    val version: String,
    val markets: List<Market>,
    val conditions: List<StrategyCondition>
)

data class StrategyCondition(val label: String, val text: String)
