package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.UserCommand
import kr.hhplus.be.server.domain.user.service.AccountCommand

class UserCriteria {
    data class CreateUserCriteria(
        val name: String,
        val email: String,
        val loginId: String,
        val password: String
    ) {
        fun toUserCommand(): UserCommand.CreateUserCommand {
            return UserCommand.CreateUserCommand(
                name = name,
                email = email,
                loginId = loginId,
                password = password
            )
        }

        fun toAccountCommand(userId: Long): AccountCommand.CreateAccountCommand {
            return AccountCommand.CreateAccountCommand(
                userId = userId,
                amount = Account.MIN_BALANCE
            )
        }
    }

    data class ChargeAccountCriteria(
        val userId: Long,
        val amount: Double
    ) {
        fun toChargeAccountCommand(accountId: Long): AccountCommand.UpdateAccountCommand {
            return AccountCommand.UpdateAccountCommand(id = accountId, amount = amount)
        }
    }
    
}
