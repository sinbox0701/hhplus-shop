package kr.hhplus.be.server.controller.user.dto.response

import java.math.BigDecimal
import java.time.LocalDateTime

data class BalanceTransactionResponse(
    val balanceId: Int,
    val accountId: Int,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 