package kr.hhplus.be.server.controller.user.dto.response

import java.time.LocalDateTime

data class UserDetailResponse(
    val id: Long,
    val name: String,
    val email: String,
    val loginId: String,
    val balance: BalanceResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BalanceResponse(
    val id: Long,
    val accountId: Long,
    val amount: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 