package kr.hhplus.be.server.controller.user.dto.response

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserDetailResponse(
    val accountId: Int,
    val name: String,
    val email: String,
    val loginId: String,
    val balance: BalanceResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BalanceResponse(
    val balanceId: Int,
    val accountId: Int,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 