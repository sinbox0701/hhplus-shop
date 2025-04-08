package kr.hhplus.be.server.controller.user.dto.response

import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val loginId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 