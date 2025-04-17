package kr.hhplus.be.server.domain.user.service

class AccountCommand {
    data class CreateAccountCommand(
        val userId: Long,
        val amount: Double
    )

    data class UpdateAccountCommand(
        val id: Long,
        val amount: Double
    )
        
}