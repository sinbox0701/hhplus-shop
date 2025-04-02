package kr.hhplus.be.server.domain.balance

import java.math.BigDecimal

data class Balance(
    val balanceId: Int,
    val accountId: Int,
    var amount: BigDecimal
)
