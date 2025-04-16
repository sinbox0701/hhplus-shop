package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.model.User

class AccountCommand {
    data class CreateAccountCommand(
        val user: User,
        val amount: Double
    )

    data class UpdateAccountCommand(
        val id: Long,
        val amount: Double
    )
        
}