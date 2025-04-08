package kr.hhplus.be.server.controller.user.dto.response

import java.time.LocalDateTime

data class BalanceTransactionResponse(
    val id: Long,
    val accountId: Long,
    val amount: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 