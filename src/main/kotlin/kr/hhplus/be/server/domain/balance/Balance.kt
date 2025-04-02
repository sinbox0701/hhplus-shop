package kr.hhplus.be.server.domain.balance

import java.math.BigDecimal

data class Balance(
    val balanceId: String,
    val accountId: String,
    var amount: BigDecimal
)
