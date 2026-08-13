package com.donghyeon.mystock.design.model

enum class AccountState { RUNNING, CLOSING, LIQUIDATING, PAUSED, WAITING }

enum class OrderSide { BUY, SELL }

enum class RunOutcome { RUNNING, CLOSED, LIQUIDATED }
