package kr.hhplus.be.server.interfaces.user

import java.time.LocalDateTime

class UserResponse {
    
    data class Response(
        val id: Long,
        val name: String,
        val email: String,
        val loginId: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
    
    data class DetailResponse(
        val id: Long,
        val name: String,
        val email: String,
        val loginId: String,
        val account: AccountResponse,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
    
    data class AccountResponse(
        val id: Long,
        val userId: Long,
        val amount: Double,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
    
    data class AccountTransactionResponse(
        val id: Long,
        val accountId: Long,
        val amount: Double,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
}
