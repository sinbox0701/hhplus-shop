package kr.hhplus.be.server.domain.user.service

import java.math.BigDecimal

class AccountCommand {
    data class CreateAccountCommand(
        val userId: Long,
        val amount: BigDecimal
    )

    data class UpdateAccountCommand(
        val id: Long,
        val amount: BigDecimal
    )

}
